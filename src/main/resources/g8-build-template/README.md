A [Giter8](http://www.foundweekends.org/giter8/) template for creating $templateDescription$

To create a new project based on the template:
==

* Install g8 commandline tool (http://www.foundweekends.org/giter8/setup.html)
* Go to the directory where you want to create the template
* Decide your service name (the hardest part :))
* Run the command

    `g8 {GITHUB_USER}/$templateName$ $g8CommandLineArgs$`
    
and then
    
    cd $testTemplateName$
    $beforeTest$
  
* Test generated project using command 

    `$testCommand$`
    

How to test the template and generate an example project 
==

* Run `./test.sh` 

An example project will be then created and tested in `$testTargetFolder$/$testTemplateName$`

How to modify the template?
==

Change the template sources blindly, 
be careful about placeholders and run `./test.sh` to validate the changes
or ... 

Run `./test.sh`, go to `$testTargetFolder$`, 
change the generated example project, 
build and test it running `$testCommand$`,
and finally run `./update-g8.sh` to port changes back to the template.

What is in the template?
==

Assuming the command above 
the template will supply the following values for placeholders:

    $placeholders$

and produce the folders and files as shown below:

    $exampleTargetTree$