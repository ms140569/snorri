package eu.schmidtm.snorriui

import eu.schmidtm.snorriui.record.Header
import eu.schmidtm.snorriui.record.loadRecord
import eu.schmidtm.snorriui.record.search
import eu.schmidtm.snorriui.storage.Storage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RecordAccessTest {
    @Test
    fun load_given_record() {

        val recordStream = this.javaClass.classLoader.getResourceAsStream("loki/sample.loki")

        assertNotNull(recordStream)

        val recordBytes = recordStream.readBytes()

        val result = loadRecord(recordBytes, "test1234")

        assertNotNull(result)

        val header = result?.first
        val payload = result?.second

        assertNotNull(header)
        assertNotNull(payload)

        assertEquals(payload?.title, "Testtitel")
        assertEquals(payload?.url, "http://www.sample.com")
    }

    fun fetch_file(path: String): Pair<Header, Storage.Record>? {

        val recordStream = this.javaClass.classLoader.getResourceAsStream(path)
        val recordBytes = recordStream.readBytes()
        return loadRecord(recordBytes, "test1234")
    }

    @Test
    fun searchTest2() {
        val result = fetch_file("loki/two.loki")
        assertNotNull(result)

        val header = result?.first
        val payload = result?.second

        assertEquals(2, (payload?.search("frumpy"))?.getSize())
    }

    @Test
    fun searchTest3() {
        val result = fetch_file("loki/three.loki")
        assertNotNull(result)

        val header = result?.first
        val payload = result?.second

        assertEquals(3, (payload?.search("gonzo"))?.getSize())
    }
}
