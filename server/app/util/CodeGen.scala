package util

object CodeGen extends App {
  slick.codegen.SourceCodeGenerator.run(
    "slick.jdbc.PostgresProfile", 
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost/onlineclassroom?user=mlewis&password=password",
    "/home/mlewis/workspaceWeb/online-quiz-and-test/server/app/", 
    "models", None, None, true, false
  )
}