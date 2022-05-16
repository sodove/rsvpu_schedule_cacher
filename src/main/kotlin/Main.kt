import okhttp3.*
import org.json.simple.JSONArray
import org.json.simple.parser.JSONParser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.*
import kotlin.random.Random

var doc: Document? = null
var groups = LinkedHashMap<String, String>()
var auds = LinkedHashMap<String, String>()
var prepod = LinkedHashMap<String, String>()

const val scheduleUrl = "https://www.rsvpu.ru/mobile/"
const val api = "https://rsvpu.ru/contents/api/rasp.php?"
const val path = "schedule_cache"
const val lists_path = "lists_cache"


var total = 1

fun main() {
    File(path).mkdir()
    File(lists_path).mkdir()
    scheduleListsParser()
}

private fun getDocument(url: String?): Document? {
    doc = Jsoup.connect(url.toString()).get()
    return doc
}

private fun saveObject(obj: LinkedHashMap<String, String>, name: String){
    val file = File(lists_path, name)
    try {
        val fos = FileOutputStream(file)
        val s = ObjectOutputStream(fos)
        s.writeObject(obj)
        s.close()
    } catch (e: Exception) {return}
}

private fun scheduleListsParser() {
    val doc = getDocument(scheduleUrl)
    val docZao = getDocument("$scheduleUrl?form=zaoch")
    var links: Elements = doc!!.select("div[name]")
    for (link in links) {
        val swName = link.attr("name")
        if (link.attr("data").isNotEmpty()) when (swName) {
            "gr" -> groups[link.text()] = link.attr("data")
            "aud" -> auds[link.text()] = link.attr("data")
            "prep" -> prepod[link.text()] = link.attr("data")
            else -> {
            }
        }
    }
    links = docZao!!.select("div[name]")
    for (link in links) {
        val swName = link.attr("name")
        if ("gr" == swName) {
            groups[link.text()] = link.attr("data")
        }
    }

    total = groups.count() + auds.count() + prepod.count()

    println("Total downloading $total schedules")

    saveObject(groups, "groups_data")
    saveObject(auds, "aud_data")
    saveObject(prepod, "prepod_data")

    for (i in groups.values.indices){
        download("v_gru=${groups.values.elementAt(i)}", groups.keys.elementAt(i))
    }
    for (i in auds.values.indices) {
        download("v_aud=${auds.values.elementAt(i)}", auds.keys.elementAt(i))
    }
    for (i in prepod.values.indices){
        download("v_prep=${prepod.values.elementAt(i)}", prepod.keys.elementAt(i))
    }
}

private fun download(data: String, name: String) {
    Thread.sleep(Random.nextLong(10,50))
    println("Downloading $name")
    lateinit var inpStream: String
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("$api$data")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                if (!response.isSuccessful) {println("fail request"); return}

                inpStream = response.body().string()
                saveSchedule(data, inpStream, name)
            }
        }
    })
}

private fun saveSchedule(data: String, inp_stream: String, name: String){
    val file = File(path, data)
    val parser = JSONParser()
    lateinit var jsonArray: JSONArray

    try {
        jsonArray = parser.parse(inp_stream) as JSONArray
    } catch (ex: Exception){
        println("Incorrect server answer")
        return
    }

    if (jsonArray.isNotEmpty()){
        val stream: FileOutputStream?
        try {
            stream = FileOutputStream(file)
        } catch (e: FileNotFoundException) {
            return
        }
        try {
            stream.write(inp_stream.toByteArray())
        } catch (e: IOException) {
            println("IOException on stream.write")
            return
        } finally {
            try {
                stream.close()
            } catch (_: IOException) {
            }
        }
    }
    else println("$name: Schedule missing, not saving")
}