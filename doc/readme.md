
Default Mode:
 no users, no rights - target Windows or Unix root user
 no configs. Think of db installation, all config is placed into db. before and after are db migrations

why:
 vertx won't need ext configuration
 easy dev run (no installation required)


===  GRADLE
 app specific:

   local, staging, production configs
   configuration reader/writer
   ConfigManager<AppConfig>, BasicConfig<T:AppConfig>: name, version, revision, build time, team, configs [:AppConfig, :AppConfig, :AppConfig]

===

java -jar my-ass.jar install --env staging

 this version =
 previous installed version =

 require {
   "java" version "^9.0"
   "badger-vcs" version "^0.0.3"
 }

 before {
 }

 install {
   requireParams {
      "app.environment"
   }

   users {

   }

   rights = {
     defaultRights = omit(),
     map(normalRights to defaultRights   //recursive
   }

   folders {
     appFolder = folder(/var/lib/$appName, normalRights.recursive(false))
     libFolder = folder($appFolder/lib)                     // unspecified, no command is called
     logFolder = folder()
     configFolder = folder(/etc/$appName)
   }

   app {
     lib = '/var/lib/$appName/lib',
     jar = '/var/lib/$appName'

     resources {   // open self as a zip file
        process("/honey/*", confFolder)
        process("/honey/badger/*", confFolder)
     }
   }

   inFolder(current) {
     script {
      id =  'my-ass'
      class = "honey.MyAss",
      env = {
        "environment" to defaultEnvironment    // the app will plug settings
      },
      args = "",
      options = setOf(
        jvmOpts("-Xmx100mb"),
        maxMemory(1000.mb)
      }
    }

    inFolder(/usr/bin) {
      link(id = 'my-ass')
      linkAllScripts()
    }


  }
  after {

     writeDefaultConfigToDb
  }
  updateApp {
    forceInstall=false
    updateScript { (thisJar, thisVersion, installedVersion) ->
        //app must contain update scripts
        //and know where it's configuration is stored

        UpdateScriptVersionXXX().run()
    }
  }
