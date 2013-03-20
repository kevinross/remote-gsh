package com.github.safrain.remotegsh.shell

import groovy.json.JsonSlurper
import jline.TerminalFactory
import jline.console.ConsoleReader
import jline.console.completer.ArgumentCompleter
import jline.console.completer.FileNameCompleter
import jline.console.completer.StringsCompleter
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.cookie.CookiePolicy
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import org.fusesource.jansi.AnsiRenderWriter
import org.fusesource.jansi.AnsiRenderer

import java.util.concurrent.Callable

/**
 * Remote Groovy Shell client
 *
 * @author safrain
 */

/**
 * Terminal input
 */
def consoleReader = null
/**
 * Terminal output
 */
def out = new AnsiRenderWriter(System.out, true)

/**
 * Shell session id (uuid)
 */
def sid = null

// ==========Configs==========
final DEFAULT_CHARSET = "utf-8"
final DEFAULT_SERVER = "http://localhost/admin/rgsh"
/**
 * Server address, with 'http://'
 */
server = System.getProperty("server") == null ? DEFAULT_SERVER : System.getProperty("server")
/**
 * Request & response charset
 */
charset = System.getProperty("charset") == null ? DEFAULT_CHARSET : System.getProperty("charset")

//Init the terminal
TerminalFactory.configure(TerminalFactory.Type.AUTO)
AnsiConsole.systemInstall()
Ansi.setDetector({ TerminalFactory.get().ansiSupported } as Callable)
try {
    consoleReader = new ConsoleReader()
} catch (e) {
    e.printStackTrace()
    System.exit(0)
}

//Init auto complete
StringsCompleter allCommands = new StringsCompleter("help", "quit", "exit", "run")
consoleReader.addCompleter(new ArgumentCompleter(new StringsCompleter("help"), allCommands))
consoleReader.addCompleter(allCommands)
consoleReader.addCompleter(new ArgumentCompleter(new StringsCompleter("run"), new FileNameCompleter()))

getResource = {
    getClass().getClassLoader().getResourceAsStream("com/github/safrain/remotegsh/shell/${it}").getText('utf-8')
}

//Define commands
COMMANDS = []
COMMANDS = [
        /help|\?/: [
                func: { args ->
                    if (args.length == 1) {
                        def c = COMMANDS.find { k, v -> args[0] ==~ k }?.value
                        if (c != null) {
                            out.println c.text
                        } else {
                            out.println getResource('help.txt')
                        }
                    }
                },
                text: 'help/help.txt'
        ],
        /exit|quit/: [
                func: { args ->
                    println("Bye~")
                    System.exit(0)
                },
                text: 'help/exit.txt'
        ]
]

//Http utilities
class ConnectionException extends RuntimeException {
    ConnectionException(Throwable throwable) {
        super(throwable)
    }
}
def client = new HttpClient()
httpGet = { String uri ->
    try {
        def get = new GetMethod(uri)
        return [
                statusCode: client.executeMethod(get),
                responseString: new String(get.getResponseBody(), charset)
        ]
    } catch (e) {
        throw new ConnectionException(e)
    }
}

httpPost = { String uri, String content ->
    try {
        PostMethod post = new PostMethod(uri)
        post.params.cookiePolicy = CookiePolicy.RFC_2109
        post.requestEntity = new StringRequestEntity(content, "text", charset)
        return [
                statusCode: client.executeMethod(post),
                responseString: new String(post.getResponseBody(), charset)
        ]
    } catch (e) {
        throw new ConnectionException(e)
    }
}

tryWithConnectionException = {
    try {
        it()
    } catch (ConnectionException e) {
        sid = null
        out.println "@|red CONNECTION PROBLEM:|@ Failed to connect to @|blue ${server}|@"
        e.getCause().printStackTrace(out)
    }
}

//Connection
ensureConnection = {
    if (sid == null) {
        tryWithConnectionException {
            def response = httpGet("${server}?r=shell")
            if (response.statusCode == 200) {
                sid = response.responseString
                out.println "@|green CONNECTED:|@ Connected to @|blue ${server}|@."
            } else {
                sid = null
                out.print "@|red ERROR:|@ Could not start shell session(@|red ${response.statusCode}|@)."
                out.println response.responseString
            }
        }

    }
}

//Show welcome screen and help
out.println getResource("welcome.txt")
out.println "@|yellow Server:|@ ${server}"
out.println "@|yellow Request charset:|@ ${charset}"
out.println getResource('help.txt')

//Ensure connection and start
ensureConnection()
while (true) {
    def input = consoleReader.readLine(AnsiRenderer.render(("@|bold rgsh@${server}>|@ " as String).trim()))
    if (!input?.trim()) {
        continue
    }
    def cmd = input.tokenize(' ')
    if (!cmd) {
        continue
    }
    def func = COMMANDS.find { k, v ->
        cmd[0] ==~ k
    }?.value?.func
    if (func) {
        cmd.remove(0)
        func cmd
    } else {
        ensureConnection()
        tryWithConnectionException {
            def response = httpPost("${server}?sid=${sid}", input)
            def r
            JsonSlurper j = new JsonSlurper()
            if (response.responseString) {
                r = j.parseText(response.responseString)
            } else {
                r = [:]
            }

            if (response.statusCode == 200 || response.statusCode == 500) {
                out.println "@|bold ===>${ r['result']}|@"
                if (r['result']) {
                    out.println r['result']
                }
            }

            if (response.statusCode == 500) {
                out.println "@|red ERROR:|@ Server exception(@|red ${response.statusCode}|@)."
                if (r['error'] != null) {
                    out.println "@|red Stack Trace:|@"
                    out.println r['error']
                }
            } else if (response.statusCode == 410) {
                sid = null
                out.println "@|red ERROR:|@ Shell session timeout(@|red \${response.statusCode|@)."
            } else {
                sid = null
                out.println "@|red ERROR:|@ Unexpected server error(@|red \${response.statusCode|@)."
                out.println response.responseString
            }
        }

    }
}
