/**
 *
 * @author http://www.turnguard.com/turnguard
 */
SemaGrowUtils = {
    StringUtils: {
        replaceAngleBrackets: function(s){
            return s.replace(/</g, function(c) { return '&lt;'; })
                    .replace(/>/g,function(c){ return '&gt;';});   
        }
    }    
};
SemaGrowPage = {
    onLoad:function(type, args, scope){
        alert("onLoad " + type + " " + args + " " + scope);
    },
    loadWelcome: function(){
        var oRequestData = {
            "localData":{
                "onSuccess":function(oRequestData, response){
                    document.getElementById("welcomeContent").innerHTML=response.responseText;
                }
            }
        };
        HttpClient.GET("welcome", oRequestData, HTTPAccept.HTML);         
    },
    login: function(){       
        var oRequestData = {
            "localData":{
                "onSuccess":function(oRequestData, response){
                    document.getElementById("auth").innerHTML=response.responseText;
                }
            }
        };           
        HttpClient.GET("auth/login", oRequestData, HTTPAccept.HTML);
    },
    authenticate: function(){       
        var oRequestData = {
            "localData":{
                "onSuccess":function(oRequestData, response){
                    document.getElementById("auth").innerHTML=response.responseText;
                },
                onError:function(oRequestData, response){
                    alert("error");
                }
            }
        };
        var currentUrl = document.location.href.replace("http://","");
        alert(currentUrl);
        var authUrl = "http://"+document.getElementById("user").value+":"+document.getElementById("pass").value+"@"+currentUrl+"auth/authenticate";
        alert(authUrl);
        HttpClient.GET(authUrl, oRequestData, HTTPAccept.HTML);
    }    
};

SemaGrowTabs = {
    createTabs:function(type, args, scope){  
        switch(args[1]){
            case SIOC.SPACE:
                scope.createSiocSpaceTabs();
            break;
        }
    },
    createSiocSpaceTabs: function(){
        /* unset tabview if defined */
        if(this.mainTabs!==undefined){
            this.mainTabs.destroy();
        }
        
        /* create tabview */
        this.mainTabs = new YAHOO.widget.TabView();
        
        /* define tabs */
        this.mainTabs.addTab( new YAHOO.widget.Tab({
            label: 'Welcome',
            id: 'welcome',
            content: '<div id=\"welcomeContent\"></div>',
            cacheData: true
        }));
        this.mainTabs.addTab( new YAHOO.widget.Tab({
            label: 'Sparql',
            id: 'sparql',
            content: '<div id=\"sparqlContent\"></div>',
            cacheData: true
        }));
        this.mainTabs.addTab( new YAHOO.widget.Tab({
            label: 'Federation',
            id: 'federation',
            content: '<div id=\"federationContent\"></div>',
            cacheData: true
        }));        
        this.mainTabs.addTab( new YAHOO.widget.Tab({
            label: 'Admin',
            id: 'admin',
            content: '<div id=\"adminContent\"></div>',
            cacheData: true
        }));
        
        /* append tabview to content */
        this.mainTabs.appendTo("content");
        
        /* tab change routine */
        this.mainTabs.on('activeTabChange', function(ev) {
            switch(ev.newValue.get("id")){
                case 'sparql':
                    SemaGrowSparql.loadSparql();
                break;
                case 'welcome':
                    SemaGrowPage.loadWelcome();
                break;
            }
        });
        
        /* activating first tab */
        this.mainTabs.set('activeIndex', 0);
    }
};

SemaGrowSparql = {
    loadSparql:function(){
        var oRequestData = {
            "localData":{
                "onSuccess":function(oRequestData, response){
                    document.getElementById("sparqlContent").innerHTML=response.responseText;
                }
            }
        };                    
        HttpClient.GET("sparql",oRequestData, HTTPAccept.HTML);         
    },
    explainSparqlQuery: function(){
        var oRequestData = {
            "localData":{
                "onSuccess":function(oRequestData, response){                    
                    document.getElementById("sparqlResponse").innerHTML="<pre>"+response.responseText+"</pre>";
                },
                onError: function(oRequestData, response){
                    document.getElementById("sparqlResponse").innerHTML=response.responseText;
                },
                "form":"sparqlQuery"
            }
        };
        HttpClient.POST("sparql/explain", oRequestData, HTTPAccept.HTML);         
    },
    runSparqlQuery: function(){
        var oRequestData = {
            "localData":{
                "onSuccess":function(oRequestData, response){
                    if(response.status===200){
                        document.getElementById("accept").value = response.getResponseHeader["Content-Type"];    
                        document.getElementById("sparqlResponse").innerHTML="<pre>"+
                                SemaGrowUtils.StringUtils.replaceAngleBrackets(response.responseText)+"</pre>"; 
                    }
                    if(response.status===204){
                        document.getElementById("sparqlResponse").innerHTML="HTTP 204 - indicating success.";
                    }
                },
                onError: function(oRequestData, response){
                    switch(response.status){
                        case 403:
                            document.getElementById("sparqlResponse").innerHTML = "HTTP 403 - You need to login.";
                        break;
                        case 401:
                            document.getElementById("sparqlResponse").innerHTML = "HTTP 401 - Your roles do not permit UPDATES/DELETES.";
                        break;
                    }                  
                },
                "form":"sparqlQuery"
            }
        };
        HttpClient.POST("sparql", oRequestData, HTTPAccept.HTML);         
    }    
};
