# Remote Groovy Shell
Remote Groovy Shell is a light weight debugging/management tool embbeded in Java servlet web applications.

You can interact with you application with groovy language in a bash like shell, jush like using groovysh.

Through this Remote Groovy Shell, you can do a lot of fantastic things to you application when its running, with the power of groovy language.

- - -
## Motivations

- I want to inspect bean properties without attach & debug to jvm
- I want to call specific method of a bean without add any fxxking jsps
- I want to mock some behavior conveniently 
- I want to modify specific property of a bean and see what happens without editing the code and restarting the application
- I want to hot replace some class

- - -

## Features

- Execute groovy command or local script file at remote server 
- Lightweighted, no additional dependencies(except groovy runtime), easy to embed into your project
- Bash like client, no manual installation required, just one command to get everything done
- Easy to extend to support various web application frameworks

- - -

## User Guide
###  Embbed rgsh in you project

#### Add *Rgsh* and *Groovy Runtime* to your classpath

Add following jars into you classpath

- [Rgsh](http://g.cn)
- [Groovy Runtime](http://g.cn)

For maven projects, add below content into you pom

	TODO

#### Configure RgshFilter

Add following filter configure into your *web.xml*

	<filter>
		<filter-name>Rgsh</filter-name>
		<filter-class>safrain.remotegsh.server.RgshFilter</filter-class>
	</filter>
	<filter-mapping>
		<filter-name>Rgsh</filter-name>
		<url-pattern>/admin/rgsh</url-pattern>
	</filter-mapping>

**Attention:Exposing this filter may cause serious security problems, make sure you have ACL on this**

### Using shell client
#### Install

TODO

#### Using interactive shell

TODO

#### Upload script and run

TODO

#### Settings

TODO

### Extending

#### Custiom init script

TODO

