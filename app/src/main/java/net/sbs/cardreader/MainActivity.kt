package net.sbs.cardreader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.sbs.cardreader.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        textTest()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
    }

    fun textTest() {
        val valueToSend = "Hello world!"
        val response = Utils.hexStringToByteArray(Utils.toHex(valueToSend.toByteArray()))
        val bytes = Utils.toHex(response).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val normalString = String(bytes, Charsets.UTF_8)
        Log.e("TAG", "textTest: $normalString")
    }

    public override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(
            this, this, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null
        )
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        Log.e("CARD", "onTagDiscovered: $tag")
        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        val response = isoDep.transceive(
            Utils.hexStringToByteArray(
                "00A4040007A0000002471001"
//                "00A404000E325041592E5359532E444446303100"
            )
        )
        Log.e("TAG", "onTagDiscovered: $response")
        runOnUiThread {
            val bytes = Utils.toHex(response).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            binding.textView.append(
                "\nCard Response: ${String(bytes, Charsets.UTF_8)}"
            )
        }
        isoDep.close()
    }

    fun String.decodeHex(): String {
        require(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
            .toString(Charsets.ISO_8859_1)  // Or whichever encoding your input uses
    }

}