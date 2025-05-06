@echo off
echo Compiling Java SSTV Decoder...
javac -cp ".;commons-math3-3.6.1.jar" Main.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed! Make sure commons-math3-3.6.1.jar is in the current directory.
    pause
    exit /b
)

echo Running SSTV Decoder...
java -cp ".;commons-math3-3.6.1.jar" Main

if %ERRORLEVEL% NEQ 0 (
    echo Execution failed! Check the error messages above.
) else (
    echo SSTV decoding completed. Check decoded_sstv.png for the result.
)

pause