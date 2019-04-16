Tool to convert your project into gitter8 template
===

## Motivation
Creating new giter8 template isn't hard, [see here](http://www.foundweekends.org/giter8/template.html), but sometimes it can be cumbersome and error-prone. 
You may also want to automate template creation after changes made to the project without manually replacing package names and text chunks with the variables. 
Use this tool from the command line or plug it into your SBT build.

## What does it mean to create g8 template?
The [giter8](http://www.foundweekends.org/giter8) template is an ordinary project folder where files or paths may contain variable placeholders, e.g. `$name$`. 
Place it on GitHub and use with the `g8` command line tool or `sbt new` command to spring your own project.

## How to run the tool from command line

    sbt "run --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [-Kkey="pattern"]"
    
    Options:
    
      -s, --source  <arg>                  Source project path
      -t, --target  <arg>                  Target template path
    
      -Kvariable=text [variable=text]...   Text chunks to parametrize
      -n, --name  <arg>                    Target template name
      -p, --package  <arg>                 Source project base package name
      -h, --help                           Show help message
      -v, --version                        Show version of this program
    

