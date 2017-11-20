# Game-of-life-Using-MPI
    Install open-mpi or mpich in your computer
## single Folder
    Main folder for the project(java)
    It can run the game on a single computer
    Download the src, and load it into the Eclipse

## mpi Folder
    MPI version of the game
    Implements using MPJ in java.


    Add mpj.jar to your java build path
        Open the Properties of your project.
        In tab Java build path->Libraries.
        Add external Jars.
        It's under the mpj-v0_44/lib folder
    Add MPJ_HOME to your system enviroment:
        export MPJ_HOME=path/to/mpj/folder
    Add MPJ_HOME to project enviroment:
        Run configurations->java application->your app->enviroment
        Create a new enviromen variable called MPJ_HOME.
        Add this MPJ_Home to the enviroment.
    Add vm argument in eclipse:
        -jar ${MPJ_HOME}/lib/starter.jar -np 4
    debug:
        -jar ${MPJ_HOME}/lib/starter.jar -np 4 -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000



## Reference
[MPI Commands](http://www.mpich.org/static/docs/latest/www/)<br>
[Open-MPI](https://www.open-mpi.org/doc/current/)<br>
[MPI-life](https://github.com/freetonik/MPI-life)<br>
[MPI-Tutotials](http://mpitutorial.com/tutorials/)<br>
[Nature of the code](http://natureofcode.com/book/chapter-7-cellular-automata/)<br>
[Draw panel using java](http://zetcode.com/gfx/java2d/basicdrawing/)<br>
[MPJ](http://mpj-express.org/docs/javadocs/index.html)<br>
[MPJ_Express_Blog](http://mpjexpress.blogspot.com)


