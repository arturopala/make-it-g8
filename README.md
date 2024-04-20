Tool to convert your project into a giter8 template
===

![Maven Central](https://img.shields.io/maven-central/v/com.github.arturopala/make-it-g8_2.13.svg) ![GitHub](https://img.shields.io/github/license/arturopala/make-it-g8.svg) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/arturopala/make-it-g8.svg)

## Demo

[Watch make-it-g8 live demonstration on YouTube.](https://youtu.be/-Gd5xGiiUtI)

## Motivation
Creating new [giter8](http://www.foundweekends.org/giter8) template is easy, but maintaining it later not as much. You have to do all the tedious placeholder replacing job again, and again, both in the content of the files and in the file paths.

The `make-it-g8` tool provides both convenient way to create new g8 template, and to update it later multiple times without effort.

## What is g8 template?
The [giter8](http://www.foundweekends.org/giter8) template is an ordinary project having nested `src/main/g8` folder where files or paths may contain placeholders, e.g. `$name$`.

Place it on GitHub and call with the `g8` command line tool or `sbt new` command to spring your own project.

## Advantages of using `make-it-g8`

* quick template creation with proper escaping of $ characters
* easy template parametrisation with multiple placeholder values
* derives automatically common placeholder variants: camel, snake, hyphen, package, packaged, etc.
* generates script to generate an example project and run test on it
* generates script updating the template after changes were made to the example project (covers full create-change-validate-update cycle)
* generates README.md with the template usage guide and an example project filetree diagram

## Prerequisites

* Java >= 8
* SBT >= 1.3.x <https://www.scala-sbt.org/release/docs/Setup.html>
* giter8 (g8) >= 0.11.0 <http://www.foundweekends.org/giter8/setup.html>
* coursier launcher <https://get-coursier.io/docs/cli-installation>

## Usage

### Consider installing the tool locally with coursier

    cs install --contrib make-it-g8

### Run the tool locally in an interactive mode

Run after installation using:

    make-it-g8

or launch using coursier:

    cs launch com.github.arturopala:make-it-g8_2.13:1.28.0 -- --interactive

or run using local clone of the repository:

    wget https://raw.githubusercontent.com/arturopala/make-it-g8/master/make-it-g8.sh
    chmod u+x make-it-g8.sh
    ./make-it-g8.sh --interactive

### Run the tool locally in a scripted mode

Run after installation using:

    make-it-g8 -- --no-interactive --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [-K key=patternUrlEncoded]

or launch using coursier:

    cs launch com.github.arturopala:make-it-g8_2.13:1.28.0 -- --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [-K key=patternUrlEncoded]

or run using local clone of the repository:

    wget https://raw.githubusercontent.com/arturopala/make-it-g8/master/make-it-g8.sh
    chmod u+x make-it-g8.sh
    ./make-it-g8.sh --source {PATH} [--target {PATH}] [--name {STRING}] [--package {STRING}] [--description {STRINGURLENCODED}] [-K key=patternUrlEncoded]

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

    libraryDependencies += "com.github.arturopala" %% "make-it-g8" % "1.28.0"      
      
## Example template created with make-it-g8

* https://github.com/arturopala/cross-scala.g8
* https://github.com/hmrc/template-play-frontend-fsm.g8

## Development

Test

    sbt test

Run locally

    sbt run

    sbt "run --interactive"

    sbt run -Dmakeitg8.interactive=true 
    

