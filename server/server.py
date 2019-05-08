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

global API, LIGHT, my_groups

class ServerHandler(BaseHTTPRequestHandler):
    global API, LIGHT, my_groups
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
            light = data['light']
            color = data['color']
            dimmer = data['dimmer']
            logging.info("Command: {}, Group: {}, Light:{}, Color: {}, Dimmer: {}".format(command, group, light, color, dimmer))
            print('salut')
            print(API)
            API(LIGHT.light_control.set_dimmer(intensity))
            self._set_response(200)
        except:
            self._set_response(400)
        
        self.wfile.write("POST request for {}\n".format(self.path).encode('utf-8'))
    
    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        try:
            if self.path.endswith("info"):
                self.wfile.write(b'Info requested.\n')
        except IOError:
            self.send_error(404, 'File Not Found: %s' %self.path)
    
        
#lance le serveur sur le port 8080 et ecoute les requetes
def run(server_class=HTTPServer, handler_class=ServerHandler, port=8080):
    global API, LIGHT, my_groups
    my_groups = {}
    
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
            gateway = Gateway()
            groups = API(gateway.get_groups())            
            if groups:
                for group in groups:
                    my_groups[API(group).name]=API(group)
                group_members = my_groups['salon'].member_ids
                group_lights = [API(gateway.get_device(group_members[i])) for i in range(0, len(group_members))]
                API(group_lights[0].light_control.set_dimmer(0))
            else:
                print("No groups found!")
                group = None
            if group_lights:
                LIGHT = group_lights[0]
            else:
                print("No lights found!")
                LIGHT = None
            moods = API(gateway.get_moods())
            if moods:
                mood = moods[0]
            else:
                print("No moods found!")
                mood = None
            tasks = API(gateway.get_smart_tasks())
            homekit_id = API(gateway.get_gateway_info()).homekit_id
            logging.info('Connected to the gateway!')
            
            data = {'group': list(my_groups.keys())[0], 'light': { 'id' : group_members[0], 'dimmer':'', 'color':'' }}
            with open('./info.json', 'w') as f:
                hjson.dump(data, f)
            
        except Exception as e:
            print(str(e))
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
