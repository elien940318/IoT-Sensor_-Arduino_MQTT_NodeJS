import serial
import time
from flask import Flask
app = Flask(__name__)


ard = serial.Serial (
    port = "COM7",
    baudrate = 9600
)
time.sleep(3)


@app.route('/')
def index():
    return "What do you want to do?</br> - /receive: receive distance</br> - /send: send LED command"

@app.route('/receive')
def receive():
    ard.write("let me know".encode())
    listen = ard.readline()
    decoded = listen.decode('utf-8')[:-2]

    return decoded

@app.route('/send')
def send():
    return "0: Off<br>1: On"

@app.route('/send/<x>')
def led_onoff(x):
    if(x == "0"):
        ard.write("0".encode())
    if(x == "1"):
        ard.write("1".encode())
    return x

if __name__ == '__main__':
    app.run(host='192.168.10.2')
