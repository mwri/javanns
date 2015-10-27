************************* JavaNNS README ***************************

Contents
--------
   1. Prerequisites
   2. Building JavaNNS
   3. Compiling Kernel interfaces
   4. NetBeans 4 support
   5. Known Bugs
   

1. Prerequisites
----------------

In order to build JavaNNS you will need:
- A current Java Development Kit (JDK) (we recommend Sun's JDK)
- A properly set up Ant Development Environment (http://ant.apache.org)


2. Building JavaNNS
-------------------

JavaNNS actually consists of two separate modules, the Java GUI and
the SNNS kernel written in C. This source code distribution of JavaNNS
supplies precompiled kernels and kernel interfaces for several
platforms.
Setting or changing the target platform does not modify the Java source
code or its compilation, but merely defines which precompiled kernel
interface will be put into the final JavaNNS.jar file.

The following list describes the targets which are implemented in out
Ant build script and their respective actions:

javanns [default] -  Compiles JavaNNS and asks for the target platform,
                     if it has not been set before. Use this target for
		         easy debugging, as all .class files will be put
		         into the respective source directories.

jar               -  Compiles JavaNNS and packages all necessary components
                     into a JAR file (dist/JavaNNS.jar)

run               -  Compiles JavaNNS (if necessary) and executes JavaNNS

settarget         -  Allows you to change the target platform
                     (Windows, Linux, Mac, Solaris)

clean             -  Deletes all intermediary .class files. Please note that
                     the dist directory won't be removed

help              -  Displays this help text



3. Building kernel interfaces
-----------------------------

This source code distribution of JavaNNS comes equipped with precompiled
kernels and kernel interfaces for

- Microsoft Windows
- Linux (x86)
- Sun Solaris
- Mac OS

So if you intend to build JavaNNS for a target not listed here or modify the
SNNS kernel, you will need the SNNS sources, which can be downloaded from

http://www-ra.informatik.uni-tuebingen.de/downloads/SNNS

Compile the kernel with an appropriate C compiler, e.g. GCC for *IX platforms
or Microsoft Visual C++ for Microsoft Windows. After this has been
accomplished, create a shared library / DLL by compiling  
javanns_KernelInterface.c from the KernelInterface directory and linking it
together with libfunc and libkernel (which were created during compilation of
the kernel) into a library.

For this to work, your compiler must be supplied with:
- the path to the include directory of your JDK which contains JNI header
  files
- and the path to the SNNS kernel source-directory (e.g. SNNS/kernel/sources)

Then overwrite the old library in the directory
   KernelInterface/precompiled/[platform]
with the newly created one.

Unfortunately, there is no easier or quicker approach available at the moment.
Please note that in order to really use this updated kernel interface, you
must copy it to the location specified in 'JavaNNS.properties', located in 
your home directory.


4. NetBeans 4 support
---------------------

We do not intend to encourage or discourage the use of a certain IDE to
perform work on JavaNNS. Anyway, Sun's NetBeans 4 allows for easy integration
of Ant based projects. We recommend to map NetBeans tasks on Ant targets as
follows:

Build Projects    ->  javanns
Clean Project     ->  clean
Run Project       ->  run
Debug Project     ->  debug


5. Known Bugs
-------------

- The "Patterns" tab in the Control Panel does not work as intended.
  Trying to add patterns or pattern sets for a network which does
  not contain any patterns so far, will lead to a fatal crash.
  Please circumvent this bug by loading patterns from a .pat file.
  With at least one pattern already loaded, this bug does not occur.
  
