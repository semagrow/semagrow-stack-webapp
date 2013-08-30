
<%@page import="java.io.File"%>
<%@page import="org.openrdf.model.impl.ValueFactoryImpl"%>
<%@page import="org.openrdf.model.BNode"%>
<%@page import="org.openrdf.model.impl.BNodeImpl"%>
<%@page import="org.openrdf.model.impl.BNodeImpl"%>
<%@page import="org.openrdf.model.Value"%>
<%@page import="org.openrdf.model.URI"%>
<%@page import="org.openrdf.model.Resource"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.openrdf.rio.RDFWriter"%>
<%@page import="java.io.Writer"%>
<%@page import="org.openrdf.query.QueryLanguage"%>
<%@page import="org.openrdf.rio.Rio"%>
<%@page import="org.openrdf.rio.RDFWriterRegistry"%>
<%@page import="java.io.OutputStream"%>
<%@page import="org.openrdf.repository.RepositoryConnection"%>
<%@page import="java.io.InputStream"%>
<%@page import="org.openrdf.repository.sail.SailRepository"%>
<%@page import="org.openrdf.sail.memory.MemoryStore"%>
<%@page import="org.openrdf.sail.Sail"%>
<%@page import="java.util.concurrent.CopyOnWriteArrayList"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Arrays"%>
<%@page import="java.util.List"%>
<%@page import="org.openrdf.rio.RDFFormat"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%!
    public void jspInit(){
        RDFRun.initialize();
    }
    public void jspDestroy(){
        RDFRun.shutdown();        
    }
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
    <%
    out.print(request.getRequestURI()+"<br/>");

    out.print(request.getPathInfo()+"<br/>");
    out.print(request.getQueryString()+"<br/>");
    out.print(request.getRequestURI()+"<br/>");
    out.print(request.getRequestURL().toString()+"<br/>");
    out.print(RDFRun.isInformationResource(request.getPathInfo())+"\n");
RDFRun.isInformationResource("").getDefaultMIMEType();
  response.setHeader("location", request.getRequestURL().toString()+"."+RDFRun.getAcceptFormats(request.getHeader("accept")).get(0).getDefaultFileExtension());
RDFWriter w = null;
HashMap<Resource, HashMap<URI, HashSet<Value>>> x = null;
Resource r = null;
String[] ss= null;
String xx = "";
BNode b = new BNodeImpl("");

for(String xxx : xx.split("")){}
while(x.size()>0){
    r = x.keySet().iterator().next();
    HashMap<URI, HashSet<Value>> m = x.remove(r);
}
response.setStatus(HttpServletResponse.SC_SEE_OTHER);
response.setHeader("location", "");
    File f = new File("");
    f.list()
    out.print(getServletContext().getResourceAsStream("/equation.rdf"));    
    %>
</html>

<%!
    private static class RDFRun {
        private static Sail memStore = new MemoryStore();
        private static SailRepository repository = new SailRepository(memStore);        
        public static void initialize(){
            try {
                repository.initialize();
            } catch(Exception e){}            
        }
        public static void shutdown(){
            try {
                repository.shutDown();
            } catch(Exception e){}            
        }        
        public static void importRDFStream(InputStream in, RDFFormat format){
            RepositoryConnection repCon = null;
            try {
                repCon = repository.getConnection();
                repCon.setAutoCommit(false);
                repCon.add(in, "", format);
                repCon.commit();
            } catch(Exception e){                
            } finally {
                if(repCon!=null){
                    try {
                        repCon.close();
                    } catch(Exception ee){}
                }
            }
        }
        public static RDFFormat isInformationResource(String requestURL){ 
            String[] splits = requestURL.split("/");            
            return RDFFormat.forFileName(splits[(splits.length-1)], RDFFormat.TURTLE);            
        }
        public static List<RDFFormat> getAcceptFormats(String acceptHeader){
            List<RDFFormat> acceptFormats = new NullFilterArrayList<RDFFormat>(); 
            for(String s : acceptHeader.split(";")){                
                acceptFormats.add(RDFFormat.forMIMEType(s));
            }
            return acceptFormats;
        }      
        public void formatDocument(String informationResourceURL, RDFFormat format, Writer out){
            RepositoryConnection repCon = null;            
            try {
                repCon = repository.getConnection();
                repCon.prepareGraphQuery(QueryLanguage.SPARQL, "DESCRIBE <"+informationResourceURL
                        .replaceFirst(("."+format.getDefaultFileExtension()), "") +">")
                        .evaluate(Rio.createWriter(format, out));                
            } catch(Exception e){                
            } finally {
                if(repCon!=null){
                    try {
                        repCon.close();
                    } catch(Exception ee){}
                }
            }                        
        }
        
        
    }
    private static class NullFilterArrayList<T> extends ArrayList<T> {
        
        @Override
        public boolean add(T e) {
            if(e!=null){
                return super.add(e);
            } else { return false; }                        
        }
             
    }
%>