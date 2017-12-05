# Game-of-life-Using-MPI
    Install open-mpi or mpich in your computer

## single Folder
    It can run the game on a single computer
    Download the src, and load it into the Eclipse

## C++ Folder
    MPI version of the game.
    Implements in C++.

## mpi Folder
    MPI version of the game
    Implements using MPJ in java.
    1.
        Create a new project in eclipse.
    2.
        Copy src directory and replace src directory in the project.
        Refresh the project in eclipse.
        Now you should see the source file in the project.
        Keep the mpj-v0_44 folder anywhere in your computer.
    3.
        Add mpj.jar to your java build path
        Open the Properties of your project.
        In tab Java build path->Libraries->Add external Jars.
        The mpj.jar is under the mpj-v0_44/lib folder.
    4.
        Create a new configuration for the project:
        Run->Run Configurations->Java application
        Doble click to create a new configuration.
        In main tab, search and select play as the main class.
    5.
        Add MPJ_HOME to project enviroment:
        In Enviroment tab, create a new enviromen variable called MPJ_HOME.
        Just locate the mpj-v0_44 folder and click to add the enviroment.
        It's not nessesary to consider the space in the directory right here.
        Add this MPJ_Home to the enviroment.
    6.
        Add vm arguments in configuration:
        -jar absolute path to mpj-v0_44 folder/lib/starter.jar -np 4
        !Attention: Please add backslash \ if your directory contain a space character.

    debug:
        -jar ${MPJ_HOME}/lib/starter.jar -np 4 -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000

    Under mpj-v0_44/doc folder, you can find the instructions about how to use the mpj.

## Reference
[MPI Commands](http://www.mpich.org/static/docs/latest/www/)<br>
[Open-MPI](https://www.open-mpi.org/doc/current/)<br>
[MPI-life](https://github.com/freetonik/MPI-life)<br>
[MPI-Tutotials](http://mpitutorial.com/tutorials/)<br>
[Nature of the code](http://natureofcode.com/book/chapter-7-cellular-automata/)<br>
[Draw panel using java](http://zetcode.com/gfx/java2d/basicdrawing/)<br>
[MPJ](http://mpj-express.org/docs/javadocs/index.html)<br>
[MPJ_Express_Blog](http://mpjexpress.blogspot.com)


