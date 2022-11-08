package com.example.payphone

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request.Method.GET
import com.android.volley.Request.Method.POST
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    var token="Kw8r6qT2C92a2PPOjzeU1EdHqEJyRjFVi0zBzl59fZFOB2qKMqwEysZFGRu-vBUj-4Vp1v2V1f1hzWpKqFoXY-KzrW7MEHzQPD0vBgQT6LI-_-fiDBigVoS8Iy_UFRf0yWbEMUOHz703eHEzO48lalBkxBtfYN62v0NSYN7ZsUZslsxQXYzayyUVzhoJUcaRyDZLm94hTIO-Hn9u_kw30-xd_uJbiKnItnM1WB2rPi9XMYIwrqjI1aL6TbXCQorCls0M-o2Js23AaOJCT9i-TXWLskSC9XBrX3XgpRpDFUAAepTQF6kO-EmdmMPx7Xyn53DHXA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var spinner=findViewById<Spinner>(R.id.spinner);
        cargar_regiones(spinner);
    }



    fun cargar_regiones(spinner: Spinner) {
        var item_regions=ArrayList<String>();
        val queue = Volley.newRequestQueue(this)
        var JsonArrayRq:()-> JsonArrayRequest =
            {   var json = object: JsonArrayRequest(GET,"https://pay.payphonetodoesposible.com/api/Regions",null,
                    { response->
                        for (i in 0  until response.length()) {
                            val Jbject: JSONObject = response.getJSONObject(i)
                            val number: String = Jbject.getString("prefixNumber")
                            item_regions.add(number)
                        }
                        spinner.setAdapter(ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_spinner_item,
                            item_regions
                        )
                        )
                    },
                    {
                    }
                ) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val headers = HashMap<String, String>()
                        headers.put("Content-Type", "application/json")
                        headers.put("Authorization", "Bearer "+token)
                        return headers
                    }
                }
                json
            }
        var json=JsonArrayRq()
        queue.add(json)
    }

    fun pagar(view: View?) {

        val numero_cel: String? = findViewById<TextView?>(R.id.phone).text.toString()
        val codigo_pais: String? = findViewById<Spinner?>(R.id.spinner).selectedItem.toString()
        val referencia: String? = findViewById<TextView?>(R.id.referencia).text.toString()
        val moneda: String? = "USD"
        var monto:Float = findViewById<TextView?>(R.id.monto).text.toString().toFloat()*100
        val clientTransactionId: String? = UUID.randomUUID().toString()

        val queue = Volley.newRequestQueue(this)
        val datos_user = object: JsonObjectRequest(GET,"https://pay.payphonetodoesposible.com/api/Users/"+numero_cel+"/region/"+codigo_pais,null,
            { response->
                val data=JSONObject()
                data.put("phoneNumber",numero_cel)
                data.put("countryCode",codigo_pais)
                data.put("clientUserId",response.getString("documentId"))
                data.put("reference",referencia)
                data.put("responseUrl","http://paystoreCZ.com/confirm.php")
                data.put("amount",monto?.toInt())
                data.put("currency",moneda)
                data.put("ClientTransactionId",clientTransactionId)
                data.put("amountWithoutTax",monto?.toInt())
                ejecuta_cobro(data);

            },
            {
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Content-Type", "application/json")
                headers.put("Authorization", "Bearer "+token)
                return headers
            }
        }
        queue.add(datos_user)
    }

    fun ejecuta_cobro(data:JSONObject){

        val cola = Volley.newRequestQueue(this)
        val request: JsonObjectRequest = object : JsonObjectRequest(
            POST,
            "https://pay.payphonetodoesposible.com/api/Sale",
            data,
            { respuesta ->
                Toast.makeText(this, respuesta.toString(),Toast.LENGTH_LONG).show()
            },
            { error -> Toast.makeText(this, "No se pudo completar", Toast.LENGTH_LONG).show() }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Content-Type", "application/json")
                headers.put("Authorization", "Bearer "+token)
                return headers
            }
        }
        cola.add(request)
    }
}