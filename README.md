Tool to convert your project into a giter8 template
===

![Build](https://github.com/arturopala/make-it-g8/workflows/Build/badge.svg) ![Maven Central](https://img.shields.io/maven-central/v/com.github.arturopala/make-it-g8_2.12.svg) ![GitHub](https://img.shields.io/github/license/arturopala/make-it-g8.svg) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/arturopala/make-it-g8.svg)

## Motivation
Creating new giter8 template isn't hard, [see here](http://www.foundweekends.org/giter8/template.html), but sometimes it can be cumbersome and error-prone. 
You may also want to automate template creation after changes made to the project without manually replacing package paths, file names and text chunks with the placeholders.

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

## Prerequisites

* Java >= 8
* SBT >= 1.3.x <https://www.scala-sbt.org/release/docs/Setup.html>
* giter8 (g8) >= 0.11.0 <http://www.foundweekends.org/giter8/setup.html>
* coursier launcher <https://get-coursier.io/docs/cli-installation>

## Usage

### Run the tool locally in interactive mode

   cs launch com.github.arturopala:make-it-g8_2.12:1.10.0 -- --interactive

   or

    wget https://raw.githubusercontent.com/arturopala/make-it-g8/master/make-it-g8.sh
    chmod u+x make-it-g8.sh
    ./make-it-g8.sh --interactive

### Run the tool locally in scripted mode

    cs launch com.github.arturopala:make-it-g8_2.12:1.10.0 -- --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [-K key=patternUrlEncoded]

    Options:

    -s, --source  <arg>                        Source code path, absolute or
                                                relative
    -p, --package  <arg>                       Source code base package name

    -c, --clear                                Clear target folder
        --noclear                              Do not clear whole target folder,
                                                only src/main/g8 subfolder
    -x, --custom-readme-header-path  <path>    Custom README.md header path
    -d, --description  <arg>                   Template description
    -f, --force                                Force overwriting target folder
        --noforce
    -i, --interactive                          Interactive mode
        --nointeractive
    -Kplaceholder=text [placeholder=text]...   Text chunks to parametrize
    -n, --name  <arg>                          Template name
    -r, --readme                               Create readme
        --noreadme                             Do not create/update readme
    -t, --target  <arg>                        Template target path, absolute or
                                                relative
    -h, --help                                 Show help message
    -v, --version                              Show version of this program
    
### Use as a library

make-it-g8 is hosted in [The Maven Central repository](https://search.maven.org/artifact/com.github.arturopala/make-it-g8/)

    libraryDependencies += "com.github.arturopala" %% "make-it-g8" % "1.10.0"      
      
## Example template created with make-it-g8

* https://github.com/hmrc/template-play-27-frontend-fsm.g8

## Development

Test

    sbt test

Run locally

    sbt run

    sbt "run --interactive"

    sbt run -Dmakeitg8.interactive=true 
    

