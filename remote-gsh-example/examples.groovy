/*
  This is Remote Groovy Shell example script
  It will show you what Remote Groovy Shell can do with a web application

  The example project is a very very simple PetStore project, you can build it and run the war file
  in any type of servlet container, or you can just run 'mvn run:jetty' to start the built-in jetty
  server and find whats going on, One thing to say is that the project contains several problems,
  which made the application unavailable, but with the power of Remote Groovy Shell and Groovy language,
  you can even fix those problems without restarting your application

  Then see the code below :D

 */

println "You can access spring application context using built-in variable '_context'"
println "\t_context = ${_context}"
println "\t_context.getClass() = ${_context.class}"

println ""

println "Then you can access your bean"
def petStore = _context.getBean('petStore')

println "\tpetStore = ${petStore}"
println "\tpetStore.getClass() = ${petStore.class}"

println ""

println "Modify bean properties"
petStore.open = false
println "\tpetStore.open = ${petStore.open}"

println ""

println "And call methods"
petStore.open()
println "\tpetStore.open = ${petStore.open}"

println ""

println "You can even create a class dynamically to override existing behaviors"
public class FixedDelivery extends com.github.safrain.remotegsh.example.Delivery {
    @Override
    public void deliver(com.github.safrain.remotegsh.example.Pet pet) {
        //Fix the BUG and do some delivery work :D
        //throw new HereIsABugException();
    }
}
petStore.delivery = new FixedDelivery()

println ""

println "Then all problems are fixed, open your browser and check it out :D"

