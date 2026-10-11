@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows shell
@setlocal enabledelayedexpansion

@if "%APP_HOME%" == "" (
    @rem Resolve links: %0 may be a link
    @set "APP_HOME=%~dp0"
    @set "APP_HOME=%APP_HOME:~0,-1%"
)

@set "APP_NAME=Gradle"
@set "APP_BASE_NAME=%~n0"

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
@set "DEFAULT_JVM_OPTS=-Xmx64m -Xms64m"

@rem Find java.exe
@if "%JAVA_HOME%" == "" (
    @set "JAVACMD=java.exe"
) @else (
    @if exist "%JAVA_HOME%\jre\bin\java.exe" (
        @set "JAVACMD=%JAVA_HOME%\jre\bin\java.exe"
    ) @else (
        @set "JAVACMD=%JAVA_HOME%\bin\java.exe"
    )
)

@if not exist "%JAVACMD%" (
    @echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
    @echo.
    @echo Please set the JAVA_HOME variable in your environment to match the
    @echo location of your Java installation.
    @goto fail
)

@set "CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar"

@rem Execute Gradle
@"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

@goto end

:fail
@echo.
@echo Build failed.
@echo.
@endlocal
@exit /b 1

:end
@endlocal