#!/usr/bin/env python3
"""
Very simple HTTP server in python for logging requests
Usage::
    ./server.py [<port>]
"""
import logging
import os
import hjson

from http.server import BaseHTTPRequestHandler, HTTPServer
from pytradfri.api.libcoap_api import APIFactory
from pytradfri.error import PytradfriError
from pytradfri.gateway import Gateway
from pytradfri.command import Command

global API, LIGHT

class ServerHandler(BaseHTTPRequestHandler):
    global API, LIGHT
    #envoi code de reponse
    def _set_response(self, code):
        self.send_response(code)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
    #recupere le json qui vient d'etre envoye via une requete POST
    def do_POST(self):
        content_length = int(self.headers['Content-Length']) # <--- Gets the size of data
        post_data = self.rfile.read(content_length) # <--- Gets the data itself
        
        data = hjson.loads(post_data.decode('utf-8'))
        #verifie le contenu du fichier json, s'il est conforme alors -->200 sinon -->400
        try:
            command = data['command']
            group = data['group']
            color = data['color']
            intensity = data['intensity']
            logging.info("Command: {}, Group: {}, Color: {}, Intensity: {}".format(command, group, color, intensity))
            print('salut')
            print(API)
            API(LIGHT.light_control.set_dimmer(intensity))
            self._set_response(200)
        except:
            self._set_response(400)
        
        
        self.wfile.write("POST request for {}".format(self.path).encode('utf-8'))

#lance le serveur sur le port 8080 et ecoute les requetes
def run(server_class=HTTPServer, handler_class=ServerHandler, port=8080):
    global API, LIGHT
    logging.basicConfig(level=logging.INFO)
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    logging.info('Starting httpd...\n')
    if os.path.exists('./gateway.conf'):
        with open('./gateway.conf') as f:
            data = hjson.load(f)
        ip = list(data.keys())[0]
        key = data[ip]['key']
        identity = data[ip]['identity']
        try:
            api_factory = APIFactory(host=ip, psk_id=identity, psk=key)
            
            API = api_factory.request
            print(API)
            gateway = Gateway()
            devices_commands = API(gateway.get_devices())
            devices = API(devices_commands)
            lights = [dev for dev in devices if dev.has_light_control]
            if lights:
                LIGHT = lights[0]
            else:
                print("No lights found!")
                LIGHT = None
            groups = API(gateway.get_groups())
            if groups:
                group = groups[0]
            else:
                print("No groups found!")
                group = None
            moods = API(gateway.get_moods())
            if moods:
                mood = moods[0]
            else:
                print("No moods found!")
                mood = None
            tasks = API(gateway.get_smart_tasks())
            homekit_id = API(gateway.get_gateway_info()).homekit_id
            logging.info('Connected to the gateway!')
        except:
            raise PytradfriError("Could not connect to the gateway. Please run configure.py before running the server.")
    else:
        raise FileNotFoundError('Please run configure.py before running the server.')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    logging.info('Stopping httpd...\n')

if __name__ == '__main__':
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()
