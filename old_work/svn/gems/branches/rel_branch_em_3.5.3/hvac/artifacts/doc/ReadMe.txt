1. This application is a starter application following the general frameworks for other enligthed applications.
2. To start the application:
     - Copy the directory at any given location.
     - Make a database enlighted_starter. Import artifacts/sql/Install.sql. This creates a default user with credential admin/admin
     - Go inside enlighted_starter directory.
     - Issue command mvn clean tomcat6:run (Yes, it's a maven application)
3. Importing as eclipse project
     - Go to enlighted_starter directory and run 'mvn clean eclipse:eclipse'
     - Import the project in eclipse.
4. This project contains the following:
     - Integrated Spring, Hibernate, Spring MVC, Spring Security and Jersey apart from others.
     - To write a page, write a controller similar to ApplicationEntryController and put a page in 
       /src/main/webapp/pages. Put an entry in tile which is same as returning string of controller.
     - For Hibernate table mapping, put the mapped Java class in com.enlightedportal.model
     - Webservics: Folloe the pattern in com.enlightedportal.ws
       
       