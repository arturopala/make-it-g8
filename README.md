Tool to convert your project into giter8 template
===

## Motivation
Creating new giter8 template isn't hard, [see here](http://www.foundweekends.org/giter8/template.html), but sometimes it can be cumbersome and error-prone. 
You may also want to automate template creation after changes made to the project without manually replacing package paths, file names and text chunks with the placeholders. 
Use this tool from the command line or plug it into your SBT build.

## What does it mean to create g8 template?
The [giter8](http://www.foundweekends.org/giter8) template is an ordinary project folder where files or paths may contain variable placeholders, e.g. `$name$`. 
Place it on GitHub and use with the `g8` command line tool or `sbt new` command to spring your own project.

## Advantages of using make-it-g8

* creates template wrapped nicely in an SBT project
* supports parametrization by multiple replacement keys
* derives automatically common key variants: camel, snake, hyphen, package, packaged, etc.
* adds a script to generate an example project and test it
* adds a script to update the template after changes made to the example project (covers full create-change-validate-update cycle)
* generates README with the template usage guide and an example project files tree picture

## Run locally

    wget https://raw.githubusercontent.com/arturopala/make-it-g8/master/make-it-g8.sh
    chmod u+x make-it-g8.sh
    ./make-it-g8.sh --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [-K key=patternUrlEncoded]
    
    Options:
    
      -s, --source  <arg>                        Source code path
      -p, --package  <arg>                       Source code base package name
    
      -t, --target  <arg>                        Template target path
      -n, --name  <arg>                          Template name
      -K placeholder=text [placeholder=text]...  Text chunks to parametrize
      
      -c, --clear                                Clear target folder
          --noclear                              Do not clear whole target folder,
                                                 only src/main/g8 subfolder
      -d, --description  <arg>                   Template description
      -r, --readme                               Create readme
          --noreadme                             Do not create/update readme
          
      -h, --help                                 Show help message
      -v, --version                              Show version of this program
      
## Example templates created with make-it-g8

* https://github.com/hmrc/template-play-26-frontend.g8
* https://github.com/hmrc/template-play-26-frontend-fsm.g8
* https://github.com/hmrc/template-play-26-microservice.g8
    

