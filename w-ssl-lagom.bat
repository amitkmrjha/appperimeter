@REM SBT launcher script
set AGENT=hostip-impl/target/universal/stage/jetty-alpn-agent/jetty-alpn-agent-2.0.9.jar
set SBT_OPTS=%SBT_OPTS% -javaagent:%AGENT%
echo Detected ALPN Agent: %SBT_OPTS%
sbt %JVM_OPTIONS% %*