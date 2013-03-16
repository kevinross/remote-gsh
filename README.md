# Remote Groovy Shell
Remote Groovy Shell is a light weight debugging/management tool embbeded in Java servlet web applications.

You can interact with you application with groovy language in a bash like shell, jush like using groovysh.

Through this Remote Groovy Shell, you can do a lot of fantastic things to you application when its running, with the power of groovy language.

## Motivations

- I want to inspect bean properties without attach & debug jvm
- I want to call specific method of a bean without add any fxxking jsps
- I want to mock some behavior conveniently 
- I want to modify specific property of a bean and see what happens without editing the code and restarting the application
- I want to hot replace some class

## Features

- Execute groovy command or local script file at remote server 
- Lightweighted, no additional dependencies(except groovy runtime), easy to embed into your project
- Bash like client, no manual installation required, just one command to get everything done
- Easy to extend to support various web application frameworks

## User Guide

### Some Examples

TODO

### Embbed rgsh in you project

1.Add following jars into you classpath

- [Remote Groovy Shell](http://g.cn) TODO
- [Groovy Runtime](http://groovy.codehaus.org/Download) Any version greater than 1.8.6 is OK 

For maven projects, add below content into you pom

	TODO

	<dependency>
		<groupId>org.codehaus.groovy</groupId>
		<artifactId>groovy-all</artifactId>
		<version>1.8.9</version>
	</dependency>
            
2.Add RgshFilter configuration into your *web.xml*

**Attention:Exposing this filter may cause serious security problems, make sure you have ACL on this**

	<filter>
		<filter-name>Rgsh</filter-name>
		<filter-class>com.github.safrain.remotegsh.RgshFilter</filter-class>
	</filter>
	<filter-mapping>
		<filter-name>Rgsh</filter-name>
		<url-pattern>/admin/rgsh</url-pattern>
	</filter-mapping>

**Filter init params**

*charset* Request and Response character encoding, 'utf-8' as default.

*initScriptPath* Init script classpath, 'safrain/remotegsh/server/init.groovy' as default.

*initScriptCharset* Init script content charset, 'utf-8' as default.


### Using shell client

Assume that you configured RgshFilter in you application at http://localhost/, '/admin/rgsh' as url pattern:

#### Show help screen

	curl -s http://localhost/admin/rgsh

Then you can follow the instructions shown on the screen.

#### Install

	curl -s http://localhost/admin/rgsh?r=install | bash

Two file will be downloaded to your current folder, an executable jar file *rgsh.jar* and a bash script  *rgsh*.

Your server host and RgshFilter charset setting will be write into *rgsh* as its default settings.

#### Starting interactive shell

	./rgsh

Then feel free to use as a common groovy shell.

Use -s switch to specify server host url.

Use -r switch to specify request charset.

#### Upload script and run

	./rgsh foobar.groovy	

Use -f switch to specify script file encoding

Use -r switch to specify request charset.

#### Default Settings

Just Edit *rgsh* and modify DEFAULT\_SERVER, DEFAULT\_FILE\_CHARSET and DEFAULT\_REQUEST\_CHARSET variable

### Extending

#### Custiom init script

TODO

