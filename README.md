# Cake-redux

Ems Administrative web application.
Cake is by the program comitee to see all talk proposals and tag them.
Cake has no database, it fetches and writes all it's data from EMS

# Login and access control
Login is done using google oath. Users are directed to google, and must login using their google account.
Authorized users are listed in the config file.

# Config file
The location of the config file must be supplied as a program argument. It has the following format:
```shell
emsEventLocation=<http location to where ems to deployed, and cake can read events.>
emsUser=<user in ems system>
emsPassword=<password in ems system>
cakeLocation=<Where you run this app>
noAuthMode=<true or false, if true the application has no authentification. Otherwise google is used>
googleClientId=<Client id to the google oath service>
googleClientSecret=<Client secret id to the google oath service>
googleRedirectUrl=http://localhost:8081/entrance
authorizedUsers=The name and email of the authorized users. Listed as : John Doe<johndoe@gmail.com>,Jane Doe<janedoe@gmail.com>
submititLocation=The location where submitit is running (used in submitit link on talk detail page). For example http://localhost:8080/talkDetail.html?talkid=
serverPort=The port where cake redux will run. For example 8081
smthost=The address to a smtp server used to send emails. For example 127.0.0.1
smtpport=The port to the smtp server. For example 25
```

# Local setup
If you want to run cake redux on your local machine you will need to have java and maven installed. Create a config file (see details above). You will at least need to spesify:
```shell
emsEventLocation=<http location to where ems to deployed, and cake can read events.>
emsUser=<user in ems system>
emsPassword=<password in ems system>
noAuthMode=true
serverPort=<Pick a free port for example 8081>
```

From project root:
```shell
mvn clean install
java -jar target/cake-redux-0.1-SNAPSHOT-jar-with-dependencies.jar <config file location>
```

Point your browser to http://localhost:<serverport>/secured/

# Licence
Copyright © 2014 javaBin

Distributed under the Eclipse Public License (http://www.eclipse.org/legal/epl-v10.html)