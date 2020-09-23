@echo off
rem set your jdk dictionary
set path=%path%;C:\Java\jdk1.8.0_151\bin
set classpath=./src
for /f "delims=" %%i in ('cd') do set dictionary=%%i
if exist %dictionary%\classes (
    echo.
) else (
    mkdir classes
)
javac -encoding UTF8 -d classes/ src/burp/burpython/core/protocol/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/core/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/UI/*.java
javac -encoding UTF8 -d classes/ src/burp/burpython/*.java
javac -encoding UTF8 -d classes/ src/burp/*.java
xcopy %dictionary%\src\burp\burpython\resource %dictionary%\classes\burp\burpython\resource /Y /I /E
cd classes && jar cvf ../burpython.jar burp