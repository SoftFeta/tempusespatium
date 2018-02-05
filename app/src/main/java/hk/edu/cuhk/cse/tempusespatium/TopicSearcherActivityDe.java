package hk.edu.cuhk.cse.tempusespatium;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Alex Poon on 2/4/2018.
 */

public class TopicSearcherActivityDe extends AppCompatActivity {

    OkHttpClient mClient;
    String mSelectedTopic;
    Map<String, String> mTopics;

    LinkedHashMap<String, String> mArts;

    AdapterView.OnItemClickListener mOnItemClickListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_searcher);

        mTopics = new HashMap<>();

        // TODO: !!!!!!!!!!!!!! https://en.wikipedia.org/wiki/Category:WikiProjects_by_topic !!!!!!!!!!!!!!!!!!!!!!!!
        // Talk, file talk...
        BootstrapButton clearButton = (BootstrapButton) findViewById(R.id.topic_clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
                autoCompleteTextView.setText("");
                autoCompleteTextView.showDropDown();
            }
        });

        BootstrapButton submitButton = (BootstrapButton) findViewById(R.id.topic_submit_button);
        submitButton.setEnabled(false);

        String base_url = "https://de.wikipedia.org/wiki/Wikipedia:Exzellente_Artikel";
        String base_url2 = "https://de.wikipedia.org/wiki/Wikipedia:Lesenswerte_Artikel";

        // NOT href="/wiki/Datei:Loudspeaker.svg" !!!

        mClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(base_url)
                .build();
        final Response[] response = {null};
        final String[] body = {""};
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = mClient.newCall(request).execute();
                    body[0] = response[0].body().string();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = (TextView) findViewById(R.id.result);
                            textView.setText(body[0]);
                            try {
                                Document doc = DocumentBuilderFactory.newInstance()
                                        .newDocumentBuilder().parse(new InputSource(new StringReader(body[0])));
                                XPathExpression staticXPath = XPathFactory.newInstance()
                                        .newXPath().compile("//*[@id=\"mw-content-text\"]/div/table/tr[2]/td/table[2]/tr[3]/td/table[2]/tr/td[1]/p[1]");
                                NodeList test = (NodeList) staticXPath.evaluate(doc, XPathConstants.NODESET);
                                for (int i = 0; i < test.getLength(); i++) {
                                    //URL
                                    Log.i("Test", test.item(i).getAttributes().item(0).getTextContent());
                                    //Topics
                                    mTopics.put(test.item(i).getTextContent().replaceFirst("Wikipedia:(WikiProject )?", "").replaceFirst("/(Popular|Most-viewed|Favourite) pages", "").replaceFirst("/Popular", "").replaceFirst("( task force| work group)", "").replaceFirst("Taskforces/(BPH/)?", "").replaceFirst("/", " > "), "https://en.wikipedia.org" + test.item(i).getAttributes().item(0).getTextContent());
//                                    Log.i("Test", test.item(i).getAttributes().item(1).getTextContent());
                                }

                            } catch (SAXException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ParserConfigurationException e) {
                                e.printStackTrace();
                            } catch (XPathExpressionException e) {
                                e.printStackTrace();
                            }
                            final ArrayAdapter<String> adapter = new ArrayAdapter<>(TopicSearcherActivityDe.this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(mTopics.keySet()));
                            AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
                            autoCompleteTextView.setAdapter(adapter);
                            autoCompleteTextView.showDropDown();
                            mOnItemClickListener = new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                    mArts = new LinkedHashMap<>();
                                    mSelectedTopic = adapter.getItem(i);
                                    final Request request1 = new Request.Builder()
                                            .url(mTopics.get(mSelectedTopic))
                                            .build();
                                    final Response[] response1 = {null};
                                    final String[] body1 = {""};
                                    Thread thread1 = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                response1[0] = mClient.newCall(request1).execute();
                                                body1[0] = response1[0].body().string();

                                                Document doc = DocumentBuilderFactory.newInstance()
                                                        .newDocumentBuilder().parse(new InputSource(new StringReader(body1[0])));
                                                XPathExpression staticXPath = XPathFactory.newInstance()
                                                        .newXPath().compile("//*[@id=\"mw-content-text\"]/div/table/tr/td[2]/a");
                                                NodeList test = (NodeList) staticXPath.evaluate(doc, XPathConstants.NODESET);
                                                for (int i = 0; i < test.getLength(); i++) {
                                                    mArts.put(test.item(i).getTextContent(), "https://en.wikipedia.org" + test.item(i).getAttributes().item(0).getTextContent());
                                                }
                                            } catch (SAXException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (ParserConfigurationException e) {
                                                e.printStackTrace();
                                            } catch (XPathExpressionException e) {
                                                e.printStackTrace();
                                            }
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    TextView textView = (TextView) findViewById(R.id.result);
                                                    textView.setText(mArts.keySet().toString());
                                                    textView.setMovementMethod(new ScrollingMovementMethod());

                                                    BootstrapButton submitButton = (BootstrapButton) findViewById(R.id.topic_submit_button);
                                                    submitButton.setEnabled(true);
                                                    submitButton.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            Intent jump = new Intent(getBaseContext(), Round1Activity.class);
                                                            jump.putExtra("topic", mSelectedTopic);
                                                            jump.putExtra("arts", mArts);
                                                            jump.putExtra("supportList", new ArrayList<>(mArts.keySet()));
                                                            startActivity(jump);
                                                            finish();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                    thread1.start();
                                }
                            };
                            autoCompleteTextView.setOnItemClickListener(mOnItemClickListener);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
