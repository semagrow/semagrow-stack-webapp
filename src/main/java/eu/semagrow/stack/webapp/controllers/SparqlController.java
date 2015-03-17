package eu.semagrow.stack.webapp.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Graph;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.parser.ParsedOperation;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.ParsedUpdate;
import org.openrdf.query.parser.QueryParserUtil;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfig;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryRegistry;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.n3.N3Writer;
import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.openrdf.rio.trig.TriGWriter;
import org.openrdf.rio.trix.TriXWriter;
import org.openrdf.rio.turtle.TurtleWriter;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.rdbms.exceptions.RdbmsQueryEvaluationException;
import org.openrdf.sail.rdbms.exceptions.UnsupportedRdbmsOperatorException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import eu.semagrow.modules.fileutils.FileUtils;
import eu.semagrow.modules.sparqlutils.SparqlUtils;
import eu.semagrow.stack.modules.api.decomposer.QueryDecompositionException;
import eu.semagrow.stack.modules.api.query.SemagrowTupleQuery;
import eu.semagrow.stack.modules.api.repository.SemagrowRepository;
import eu.semagrow.stack.modules.commons.CONSTANTS;
import eu.semagrow.stack.modules.sails.semagrow.config.SemagrowRepositoryConfig;

/**
 *
 * @author http://www.turnguard.com/turnguard
 * @author Giannis Mouchakis
 */
@Controller
@RequestMapping("/sparql")
public class SparqlController {
    
    private SemagrowRepository repository;
    
    public SparqlController() throws RepositoryException {            
    }
    
    @PostConstruct
    public void startUp() throws RepositoryException, RepositoryConfigException {
        SemagrowRepositoryConfig repoConfig = getConfig();
        RepositoryFactory repoFactory = RepositoryRegistry.getInstance().get(repoConfig.getType());
        repository = (SemagrowRepository) repoFactory.getRepository(repoConfig);
        repository.initialize();
    }
    
    @PreDestroy
    public void shutDown(){
        try {
            this.repository.shutDown();
        } catch (RepositoryException ex) {}
    }

    private SemagrowRepositoryConfig getConfig() {

        try {
            File file = FileUtils.getFile("repository.ttl");
            Graph configGraph = parseConfig(file);
            RepositoryConfig repConf = RepositoryConfig.create(configGraph, null);
            repConf.validate();
            RepositoryImplConfig implConf = repConf.getRepositoryImplConfig();
            return (SemagrowRepositoryConfig)implConf;
        } catch (RepositoryConfigException e) {
            e.printStackTrace();
            return new SemagrowRepositoryConfig();
        } catch (SailConfigException | IOException | NullPointerException e) {
            e.printStackTrace();
            return new SemagrowRepositoryConfig();
        }
    }

    protected Graph parseConfig(File file) throws SailConfigException, IOException {

        RDFFormat format = Rio.getParserFormatForFileName(file.getAbsolutePath());
        if (format==null)
            throw new SailConfigException("Unsupported file format: " + file.getAbsolutePath());
        RDFParser parser = Rio.createParser(format);
        Graph model = new GraphImpl();
        parser.setRDFHandler(new StatementCollector(model));
        InputStream stream = new FileInputStream(file);

        try {
            parser.parse(stream, file.getAbsolutePath());
        } catch (Exception e) {
            throw new SailConfigException("Error parsing file!");
        }

        stream.close();
        return model;
    }

    @RequestMapping(method=RequestMethod.GET)
    public ModelAndView getSparqlEndpointForm(HttpServletResponse response) throws IOException{
        ModelAndView mav = new ModelAndView("sparqlendpoint");
        return mav;
    }
    
    @RequestMapping(value="", params={ CONSTANTS.WEBAPP.PARAM_QUERY })
    public void query(HttpServletResponse response, HttpServletRequest request, 
            @RequestParam String query, 
            @RequestParam(defaultValue="") String prefixes,
            @RequestParam(value=CONSTANTS.WEBAPP.PARAM_ACCEPT, defaultValue="", required=false) String accept) 
            throws MalformedQueryException, RepositoryException, UpdateExecutionException, IOException, QueryEvaluationException, TupleQueryResultHandlerException, RDFHandlerException {
        
        if(!prefixes.trim().equals("")){
            query = prefixes.concat("\n").concat(query);
        }
        
        RepositoryConnection repCon = null;
        
        try {
            ParsedOperation pO = QueryParserUtil.parseOperation(QueryLanguage.SPARQL, query, null);
            if(pO instanceof ParsedUpdate){
                if(request.getUserPrincipal()!=null && request.isUserInRole(CONSTANTS.WEBAPP.ROLES.ROLE_SEMAGROW_ADMIN)){
                    repCon = this.repository.getConnection();                    
                    repCon.begin();
                    repCon.prepareUpdate(QueryLanguage.SPARQL, query).execute();                    
                    repCon.commit();                    
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    if(request.getUserPrincipal()==null){
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }
            }
            if(pO instanceof ParsedQuery){
                repCon = this.repository.getConnection();
                Query q = repCon.prepareQuery(QueryLanguage.SPARQL, query);
                String acceptFormat = !accept.equals("")?accept:request.getHeader("accept");
                accept = SparqlUtils.getAcceptMimeType(q, acceptFormat);
                response.setContentType(accept);
                this.handleQuery(response, accept, q);
            }            
        } finally {
            if(repCon!=null){
                repCon.close();
            }
        }
    }
            
    @RequestMapping(value="/explain", method=RequestMethod.POST, params={ CONSTANTS.WEBAPP.PARAM_QUERY })
    public void explain(HttpServletResponse response, HttpServletRequest request, @RequestParam String query, @RequestParam(defaultValue="") String prefixes) 
            throws IOException, RepositoryException, RepositoryException, MalformedQueryException {
        if(!prefixes.trim().equals("")){
            query = prefixes.concat("\n").concat(query);
        }
        
        response.setContentType("text/plain");
        RepositoryConnection repCon = null;
        try {
            repCon = this.repository.getConnection();
            ParsedOperation pO = QueryParserUtil.parseOperation(QueryLanguage.SPARQL, query, null);
            if(pO instanceof ParsedUpdate){
                response.getWriter().append(repCon.prepareUpdate(QueryLanguage.SPARQL, query).toString());
            }
            if(pO instanceof ParsedQuery){
                response.getWriter().append(repCon.prepareQuery(QueryLanguage.SPARQL, query).toString());
            }            
            response.getWriter().flush();
        } finally {
            if(repCon!=null){
                repCon.close();
            }
        }
    }


    @RequestMapping(value="/decompose", method=RequestMethod.POST, params={ CONSTANTS.WEBAPP.PARAM_QUERY })
    public void decompose(HttpServletResponse response, HttpServletRequest request, @RequestParam String query, @RequestParam(defaultValue="") String prefixes)
            throws IOException, RepositoryException, RepositoryException, MalformedQueryException {
        if(!prefixes.trim().equals("")){
            query = prefixes.concat("\n").concat(query);
        }

        response.setContentType("text/plain");
        RepositoryConnection repCon = null;
        try {
            repCon = this.repository.getConnection();
            ParsedOperation pO = QueryParserUtil.parseOperation(QueryLanguage.SPARQL, query, null);
            if(pO instanceof ParsedUpdate){
                response.getWriter().append(repCon.prepareUpdate(QueryLanguage.SPARQL, query).toString());
            }
            if(pO instanceof ParsedQuery){
                Query q = repCon.prepareQuery(QueryLanguage.SPARQL, query);
                if (q instanceof SemagrowTupleQuery) {
                    try {
                        TupleExpr decomposedExpr = ((SemagrowTupleQuery) q).getDecomposedQuery();
                        response.getWriter().append(decomposedExpr.toString());
                    } catch (QueryDecompositionException e) {
                        response.getWriter().append("Query cannot be decomposed");
                    }
                }
            }
            response.getWriter().flush();
        } finally {
            if(repCon!=null){
                repCon.close();
            }
        }
    }
    
    @RequestMapping(value="/reloadConfig", method=RequestMethod.GET)
    public void reloadConfig(HttpServletResponse response, HttpServletRequest request) 
            throws IOException, RepositoryConfigException, RepositoryException, MalformedQueryException {
        if(request.getUserPrincipal()!=null && request.isUserInRole(CONSTANTS.WEBAPP.ROLES.ROLE_SEMAGROW_ADMIN)){
            this.startUp();                    
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
        } else {
            if(request.getUserPrincipal()==null){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
    }    
    
    private void handleQuery(HttpServletResponse response, String accept, Query query) 
    		throws IOException  {
    	OutputStream out = response.getOutputStream();
    	try {
			if (query instanceof TupleQuery) {
				if (accept.indexOf(CONSTANTS.MIMETYPES.SPARQLRESULTS_XML) != -1) {
					((TupleQuery) query).evaluate(new SPARQLResultsXMLWriter(out));
				} else if (accept.indexOf(CONSTANTS.MIMETYPES.SPARQLRESULTS_JSON) != -1) {
					((TupleQuery) query).evaluate(new SPARQLResultsJSONWriter(out));
				} else {
					((TupleQuery) query).evaluate(new eu.semagrow.modules.rioutils.queryresultio.HTMLTableWriter(out));
				}
			} else if (query instanceof GraphQuery) {
				if (accept.indexOf(CONSTANTS.MIMETYPES.RDF_RDFXML) != -1) {
					((GraphQuery) query).evaluate(new RDFXMLPrettyWriter(out));
				} else if (accept.indexOf(CONSTANTS.MIMETYPES.RDF_N3) != -1) {
					((GraphQuery) query).evaluate(new N3Writer(out));
				} else if (accept.indexOf(CONSTANTS.MIMETYPES.RDF_TURTLE) != -1) {
					((GraphQuery) query).evaluate(new TurtleWriter(out));
				} else if (accept.indexOf(CONSTANTS.MIMETYPES.RDF_TRIG) != -1) {
					((GraphQuery) query).evaluate(new TriGWriter(out));
				} else if (accept.indexOf(CONSTANTS.MIMETYPES.RDF_TRIX) != -1) {
					((GraphQuery) query).evaluate(new TriXWriter(out));
				} else {
					((GraphQuery) query).evaluate(new TurtleWriter(out));
				}
			} else if (query instanceof BooleanQuery) {
				out.write((((BooleanQuery) query).evaluate() + "").getBytes());
			}
    	} catch (TupleQueryResultHandlerException e) {
    		response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
		} catch (QueryEvaluationException e) {
			if (e instanceof QueryInterruptedException) {
				response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);			
			} else if (e instanceof HTTPQueryEvaluationException) {
				if (((HTTPQueryEvaluationException) e).isCausedByIOException()) {
					response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
				} else if (((HTTPQueryEvaluationException) e).isCausedByMalformedQueryException()) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				} else if (((HTTPQueryEvaluationException) e).isCausedByRepositoryException()) {
					response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
				} else {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else if (e instanceof RdbmsQueryEvaluationException) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} else if (e instanceof UnsupportedRdbmsOperatorException) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else if (e instanceof ValueExprEvaluationException) {
				response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			} else if (e.getCause() instanceof RepositoryException || e.getCause() instanceof IOException) {
				response.setStatus(HttpServletResponse.SC_GATEWAY_TIMEOUT);
				response.setHeader("ERROR", "Time-out while quering a source.\n"
						+ "Please contact the source's administrator to get more info");
				response.flushBuffer();
			} else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (RDFHandlerException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
 	
    }    
}
