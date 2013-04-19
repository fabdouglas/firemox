:restart
java -Xmx512M -XX:MaxPermSize=256M -jar starter.jar
@if %ERRORLEVEL%==47 GOTO restart