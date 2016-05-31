package gr.mybook.mybt_2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
    private Handler repeatUpdateHandler=new Handler();
    private boolean mAutoIncrement=false;
    private boolean mAutoDerement=false;

    // AOA protocol for temperature sensor
	private static final byte COMMAND_TEMP = 0x4;
	// AOA protocol forLED
	private static final byte COMMAND_LED = 0x2;
	private static final byte TARGET_LED = 0x2;
	private static byte TOGGLE = 0xf;


    //Motor
    private static final byte COMMAND_MOTOR = 0x5;

    private static final byte TARGET_FRONT_MOTOR = 0x6;
    private static final byte TURN_RIGHT = 0x7;
    private static final byte TURN_LEFT = 0x8;
    private  byte SPEED=0;
    private  byte Turn=0;


    private static final byte TARGET_BACK_MOTOR = 0xa;
    private static final byte FRONT = 0xb;
    private static final byte BACK = 0xc;
    private static final byte STOP = 0xd;







    //End Motor

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE=1;

    // Messages sent to Handler from Threads
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_FIELD = 3;
    public static final int MESSAGE_READ = 4;
    public static final int MESSAGE_WRITE = 5;

    // Key names to Handler from Threads
    public static final String DEVICE_NAME = "device_name";
    public static final String FIELD = "field";
    public static final String READ = "read";
    public static final String WRITE = "write";

	// Return Intent extra
    public static String DEVICE_ADDRESS = "device_address";

	ImageButton ibtn;;
	ToggleButton tbtn;
	TextView txt1, txt2;
    Button frontButton,backButton,leftButton,rightButton;

	private BluetoothAdapter mBluetoothAdapter=null;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ibtn=(ImageButton) findViewById(R.id.imageButton1);
        txt1=(TextView) findViewById(R.id.textView1);
        txt2=(TextView) findViewById(R.id.textView2);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        frontButton=(Button)findViewById(R.id.Front_button);
        backButton=(Button)findViewById(R.id.Back_button);
        rightButton=(Button)findViewById(R.id.Right_button);
        leftButton=(Button)findViewById(R.id.Left_Button);



        frontButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SPEED = 50;
                    Front();

                }
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL && mAutoIncrement) {
                    mAutoIncrement = false;
                    SPEED = 0;
                    Front();


                }
                return false;
            }
        });
        frontButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RptUpdater());
                return true;
            }
        });



        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SPEED = 50;
                    Back();

                }


                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL && mAutoIncrement) {
                    mAutoIncrement = false;
                    SPEED = 0;
                    Back();

                }

                return false;
            }
        });

        backButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RptBack());
                return true;
            }
        });

        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Turn = 120;
                    Left();

                }
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL && mAutoIncrement) {
                    mAutoIncrement = false;
                    Left();

                }

                return false;
            }
        });


        leftButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RptLeft());
                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    Turn = 120;
                    Right();

                }
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL && mAutoIncrement) {
                    mAutoIncrement = false;
                    Right();

                }

                return false;
            }
        });

        rightButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RptRight());
                return true;
            }
        });



    }


    public  void Front(){
        byte[] buffer = new byte[4];
        buffer[0]= SPEED;
        buffer[1] = COMMAND_MOTOR;
        buffer[2] = TARGET_BACK_MOTOR;
        buffer[3] = FRONT;
        mConnectedThread.write(buffer);

    }
    public void Back(){
        byte[] buffer = new byte[4];
        buffer[0]= SPEED;
        buffer[1] = COMMAND_MOTOR;
        buffer[2] = TARGET_BACK_MOTOR;
        buffer[3] = BACK;
        mConnectedThread.write(buffer);

    }
    public void Right(){
        byte[] buffer = new byte[4];
        buffer[0]= Turn;
        buffer[1] = COMMAND_MOTOR;
        buffer[2] = TARGET_FRONT_MOTOR;
        buffer[3] = TURN_RIGHT;
        mConnectedThread.write(buffer);
    }

    public void Left(){
        byte[] buffer = new byte[4];
        buffer[0]= Turn;
        buffer[1] = COMMAND_MOTOR;
        buffer[2] = TARGET_FRONT_MOTOR;
        buffer[3] = TURN_LEFT;
        mConnectedThread.write(buffer);
    }


    @Override
    public void onResume() {
      super.onResume();
    }

    @Override
    public void onPause() {
      super.onPause();
    }

	@Override
	public void onDestroy() {
        super.onDestroy();
		stop();
	}

    public void search(View view) {
    	Intent intent = new Intent(this,DeviceSearchActivity.class);
    	startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }

    public void onoff(View view) {
    	byte[] buffer = new byte[3];
    	buffer[0] = COMMAND_LED;
    	buffer[1] = TARGET_LED;
    	buffer[2] = TOGGLE;
    	mConnectedThread.write(buffer);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (requestCode) {
    	case REQUEST_CONNECT_DEVICE:
    		if (resultCode == Activity.RESULT_OK) {
    		  // Get the device MAC address
              String address = data.getExtras().getString(DEVICE_ADDRESS);
              // Get the BLuetoothDevice object
              BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
              // Cancel any thread currently running a connection
              if (mConnectedThread != null) {
            	mConnectedThread.cancel();
            	mConnectedThread = null;
              }
              // Attempt to connect to the device
              mConnectThread = new ConnectThread(device);
              mConnectThread.start();
    		}
    	}
    }

    // Management of BT connection through ConnectedThread
    public void connected(BluetoothSocket socket, BluetoothDevice device) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
        	mConnectThread.cancel();
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    // Stop all threads
    public void stop() {

        if (mConnectThread != null) {
        	mConnectThread.cancel();
        	mConnectThread = null;
        }

        if (mConnectedThread != null) {
        	mConnectedThread.cancel();
        	mConnectedThread = null;
        }
    }

    // Connection attempt failed
    private void connectionFailed() {

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_FIELD);
        Bundle bundle = new Bundle();
        bundle.putString(FIELD, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    // Connection was lost
    private void connectionLost() {

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_FIELD);
        Bundle bundle = new Bundle();
        bundle.putString(FIELD, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

 /*   public void Front(View view) {
        byte[] buffer = new byte[4];
        buffer[0]=SPEED;
        buffer[1] = COMMAND_MOTOR;
        buffer[2] = TARGET_BACK_MOTOR;
        buffer[3] = FRONT;
        mConnectedThread.write(buffer);
    }*/

/*
    public void Left(View view) {
        byte[] buffer = new byte[3];
        buffer[0] = COMMAND_MOTOR;
        buffer[1] = TARGET_FRONT_MOTOR;
        buffer[2] = TURN_LEFT;
        mConnectedThread.write(buffer);
    }
*/

/*    public void Right(View view) {
        byte[] buffer = new byte[3];
        buffer[0] = COMMAND_MOTOR;
        buffer[1] = TARGET_FRONT_MOTOR;
        buffer[2] = TURN_RIGHT;
        mConnectedThread.write(buffer);
    }*/

   /* public void Back(View view) {
        byte[] buffer = new byte[3];
        buffer[0] = COMMAND_MOTOR;
        buffer[1] = TARGET_BACK_MOTOR;
        buffer[2] = BACK;
        mConnectedThread.write(buffer);
    }*/



    public void Straight(View view) {
        Turn=0;
        Right();
        Left();
    }

    // Thread for connection with remote BT device
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {

            // Always cancel discovery because it will slow down a connection
        	mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) { }
                return;
            }

            // Reset the ConnectThread-connection ok
            synchronized (MainActivity.this) {
                mConnectThread = null;
            }
             // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    // Manage connection with BT device - in and out data
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        // Write to the connected OutStream
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) { }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    // The Handler that gets information back 
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_WRITE:
                break;
            case MESSAGE_READ:
                byte[] buffer = (byte[]) msg.obj;
                int length=msg.arg1;
    			if (buffer[0] == COMMAND_TEMP) {
     			  final int temp = (((buffer[2] & 0xff) << 24)
    					  + ((buffer[3] & 0xff) << 16)
    					  + ((buffer[4] & 0xff) << 8)
    					  + (buffer[5] & 0xff));
    			  String mytext=Integer.toString(temp);
                    txt2.setText(""+mytext);

    			}
    			break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                txt1.setText(msg.getData().getString(DEVICE_NAME));
                int bgrnd = getResources().getColor(R.color.green);
                ibtn.setBackgroundColor(bgrnd);
                break;
            case MESSAGE_FIELD:
            	Toast.makeText(MainActivity.this, msg.getData().getString(FIELD), Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

 class RptUpdater implements Runnable{

        @Override
        public void run() {
            if (mAutoIncrement){
                increment();
                Front();
                repeatUpdateHandler.postDelayed(new RptUpdater(),400);
            }else if(mAutoDerement){

                decrement();
                Front();
                repeatUpdateHandler.postDelayed(new RptUpdater(),400);

            }
        }
    }
    class RptBack implements Runnable {

        @Override
        public void run() {
            if (mAutoIncrement) {
                increment();
                Back();
                repeatUpdateHandler.postDelayed(new RptBack(), 400);
            } else if (mAutoDerement) {

                decrement();
                Back();
                repeatUpdateHandler.postDelayed(new RptBack(), 400);

            }
        }
    }

    class RptLeft implements Runnable {

        @Override
        public void run() {
            if (mAutoIncrement) {
              //  incrementturn();
                Left();
                repeatUpdateHandler.postDelayed(new RptLeft(), 400);
            } else if (mAutoDerement) {

                decrement();
                Left();
                repeatUpdateHandler.postDelayed(new RptLeft(), 400);

            }
        }
    }
    class RptRight implements Runnable {

        @Override
        public void run() {
            if (mAutoIncrement) {
               // incrementturn();
                Right();
                repeatUpdateHandler.postDelayed(new RptRight(), 400);
            } else if (mAutoDerement) {

                decrement();
                Right();
                repeatUpdateHandler.postDelayed(new RptRight(), 400);

            }
        }
    }

    public void increment(){
        if (SPEED<120) {
            SPEED +=10;
        }
            //frontButton.setText(SPEED);
    }

    public void decrement(){
        SPEED--;
        //frontButton.setText(SPEED);
    }
    public void incrementturn(){
        if (TURN_RIGHT<120) {
            Turn +=10;
        }
        //frontButton.setText(SPEED);
    }




}
