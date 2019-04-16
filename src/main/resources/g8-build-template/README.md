A [Giter8](http://www.foundweekends.org/giter8/) template for creating $templateDescription$

To create a new project:
==

* Install g8 commandline tool (http://www.foundweekends.org/giter8/setup.html)
* Go to the directory where you want to create the template
* Decide your service name (the hardest part :))
* To create a generic microservice run the command

    `g8 {GITHUB_USER}/$templateName$ $g8CommandLineArgs$`
  
* The new project folder will be created
* Change working directory to the new one
* Init git repo and do initial commit
* Test generated project using command 

    `$testCommand$`
    

To test the template itself  
==

* Run `./test.sh` 

Temporary services will be then created and tested in `target/sandbox/$templateName$`

Template content
==

Assuming the command above 
the template will supply the following values for placeholders:

    $placeholders$

and produce the folders and files as shown below:

    $exampleTargetTree$