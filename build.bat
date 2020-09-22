@echo off
rem set your jdk dictionary
set path=%path%;C:\Java\jdk1.8.0_151\bin
set classpath=./src
for /f "delims=" %%i in ('cd') do set dictionary=%%i
javac -encoding UTF8 -d classes/ src/burp/burpython/core/protocol/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/core/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/UI/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/*.java
javac -encoding UTF8 -d classes/ src/burp/*.java
copy %dictionary%\src\burp\burpython\core\template.py %dictionary%\classes\burp\burpython\core\template.py /Y
copy %dictionary%\src\burp\burpython\core\burpython.py %dictionary%\classes\burp\burpython\core\burpython.py /Y
cd classes && jar cvf ../burpython.jar burp