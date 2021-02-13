$templateDescription$
===

A [Giter8](http://www.foundweekends.org/giter8/) template for creating $templateDescription$

$customReadmeHeader$

How to create a new project based on the template?
---

* Go to directory where you want to create the template
* Decide your project name (the hardest part :))
* Run the command

    `sbt new {GITHUB_USER}/$templateName$`

or    

* Install g8 commandline tool (http://www.foundweekends.org/giter8/setup.html)
* Run the command

    `g8 {GITHUB_USER}/$templateName$ $g8CommandLineArgs$`
    
and then
    
    cd $testTemplateName$
    $beforeTest$
  
* Test generated project using command 

    `$testCommand$`
    

How to test the template and generate an example project?
---

* Run `./test.sh` 

An example project will be then created and tested in `$testTargetFolder$/$testTemplateName$`

How to modify the template?
---

 * review template sources in `/src/main/g8`
 * modify files as you need, but be careful about placeholders, paths and so on
 * run `./test.sh` in template root to validate your changes
 
or (safer) ...

* run `./test.sh` first
* open `$testTargetFolder$/$testTemplateName$` in your preferred IDE, 
* modify the generated example project as you wish, 
* build and test it as usual, you can run `$testCommand$`,
* when you are done switch back to the template root
* run `./update-g8.sh` in order to port your changes back to the template.
* run `./test.sh` again to validate your changes

What is in the template?
--

Assuming the command above 
the template will supply the following values for the placeholders:

    $placeholders$

and produce the folders and files as shown below:

    $exampleTargetTree$