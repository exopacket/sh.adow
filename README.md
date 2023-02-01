# sh.adow

<b>Work in progress</b>

The built .jar is located in the target directory. Use the file labeled 'with-dependencies'<br><br>
To initialize the configuration run<br>
`java -jar shadow.jar cli configure-defaults`<br>

To run the interactive shell<br>
`java -jar shadow.jar cli capture`<br>

# To-do

- [ ] create interactive interface for listing 'last' commands by ranges
- [ ] check if package is already installed, available, and print more information about installation
- [ ] write logic for variables
- [ ] rewrite prompts for the 'branch' command
- [ ] write logic for showInfo() function
- [ ] write help command output
- [ ] add commands to skip in config (will not skip if output is redirected)
- [ ] branch merge feature / generate shell script for project
- [ ] generate archive for offline projects
- [ ] write shell code wrappers for the java application

# Interactive Shell usage

Type these commands into the interactive shell to fine tune how your terminal session is tracked. <i>Some may be non-functional currently</i>

`ignore` begin ignoring commands entered into the terminal<br>
`continue` stop ignoring and keep track of commands<br>
`forget` erase every command history in the current branch<br>
`edit` open a file in your preferred text editor for editing and store the result<br>
`install` install a package (downloads and stores with offline flag set). Assumes that the package name is correct. Must run .jar as root user or have appropriate sudo rights w/o a passwsord.<br>
`last [n]` iterate through the previous commands<br>
`export|var` set an environment variable<br>
`branch` end current branch and start a new one<br>
`exit` exit the interactive terminal<br>
`help` list these commands and their information<br>