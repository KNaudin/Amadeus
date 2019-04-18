import os
import sys
import threading
import time
import datetime
import hjson

from PyQt4 import QtGui, QtCore, Qt, uic

DIRECTORY = os.path.dirname(__file__)

class mirror(QtGui.QMainWindow):
    def __init__(self):
        super(mirror, self).__init__()
        uic.loadUi(os.path.join(DIRECTORY, 'mirror.ui'), self)
        self.showFullScreen()
        self.stop_thread = threading.Event()
        self.w_thread = threading.Thread(target=self.check_for_weather, args=(self.stop_thread,))
        self.w_thread.start()
        self.updateClock()
        self.clocktimer = QtCore.QTimer()
        self.clocktimer.timeout.connect(self.updateClock)
        self.clocktimer.start(1000)

    def updateClock(self):
        dateinfo = datetime.datetime.now()
        self.hour_lcd.display(dateinfo.hour)
        self.minute_lcd.display(dateinfo.minute)
        self.second_lcd.display(dateinfo.second)

    def check_for_weather(self, stop_thread):
        while True and not stop_thread.isSet():
            if os.path.exists(os.path.join(DIRECTORY, '../weather.json')):
                with open(os.path.join(DIRECTORY, '../weather.json')) as f:
                    data = hjson.load(f)
                self.temp_lcd.display(float(data['main']['temp']))
                self.hum_lcd.display(float(data['main']['humidity']))
                self.cloud_lcd.display(float(data['clouds']['all']))
                self.wind_lcd.display(float(data['wind']['speed']))
            time.sleep(60)

    def closeEvent(self, event):
        self.stop_thread.set()
        event.accept()


if __name__ == '__main__':
    app = QtGui.QApplication(sys.argv)
    MIRRORUI = mirror()
    palette = MIRRORUI.palette()
    palette.setColor(MIRRORUI.backgroundRole(), QtGui.QColor(0, 0, 0))
    MIRRORUI.setPalette(palette)
    cursor = QtGui.QCursor()
    cursor.setShape(10)
    MIRRORUI.setCursor(cursor)
    sys.exit(app.exec_())
