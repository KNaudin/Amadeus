#!/usr/bin/env python3

import logging
import os
import hjson
import json

from http.server import BaseHTTPRequestHandler, HTTPServer
from pytradfri.api.libcoap_api import APIFactory
from pytradfri.error import PytradfriError
from pytradfri.gateway import Gateway
from pytradfri.command import Command

global API, LIGHT, my_groups, my_group_members, gateway, device_info

class ServerHandler(BaseHTTPRequestHandler):
    global API, my_groups, my_group_members, gateway, device_info
    
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
            group_name = data['group']
            light_id = data['light']
            color_value = data['color']
            dimmer_value = data['dimmer']
            logging.info("Command: {}, Group: {}, Light:{}, Color: {}, Dimmer: {}".format(command, group_name, light_id, color_value, dimmer_value))
            group = None
            group_lights = None
            light = None
            groups = []
            lights = []

            
            #traitement de la requette POST
            if group_name:
                group = my_groups[group_name]
                group_lights = [API(gateway.get_device(my_group_members[group_name][i])) for i in range(0, len(my_group_members[group_name]))]
            if light_id:
                light = API(gateway.get_device(light_id))
            if command=="on":
                if (group and light) or light:
                    API(light.light_control.set_state(1))
                elif group:
                    API(group.set_state(1))
            elif command=="off" or dimmer_value == 0:
                if (group and light) or light:
                    API(light.light_control.set_state(0))
                elif group:
                    API(group.set_state(0))
            elif dimmer_value:
                #set dimmer: value
                if (group and light) or light:
                    API(light.light_control.set_dimmer(dimmer_value))
                if group:
                    for i in range(0, len(group_lights)):
                        API(group_lights[i].light_control.set_dimmer(dimmer_value))
            if color_value:
                #set hex_color: value
                if (group and light) or light:
                    API(light.light_control.set_hex_color(color_value))
                elif group:
                    for i in range(0, len(group_lights)):
                        API(group_lights[i].light_control.set_hex_color(color_value))
            self._set_response(200)
        except Exception as e:
            logging.info(str(e))
            self._set_response(400)
        
        for group in device_info['groups']:
            for light in group['lights']:
                if light['id']==light_id or group['id']==group_name:
                    if dimmer_value:
                        light['dimmer'] = dimmer_value
                    elif color_value:
                        light['color'] = color_value
                lights.append(light)
            group['lights']=lights
            groups.append(group)
        device_info['groups']=groups

        self.wfile.write("POST request for {}\n".format(self.path).encode('utf-8'))
    
    #recupere les requetes GET et renvoi un fichier json si elle fini par /info
    def do_GET(self):
        self.send_response(200)
        self.end_headers()
        try:
            if self.path.endswith("info"):
                self.wfile.write(json.dumps(device_info).encode())
        except IOError:
            self.send_error(404, 'File Not Found: %s' %self.path)
    
        
#lance le serveur sur le port 8080 et ecoute les requetes
def run(server_class=HTTPServer, handler_class=ServerHandler, port=8080):
    global API, my_groups, my_group_members, gateway, device_info
    my_groups = {}
    my_group_members = {}
    device_info = {'groups':[]}
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
                    my_groups[API(group).name] = API(group) #creation dictionnaire {group_name : group}
                    my_group_members[API(group).name] = API(group).member_ids #creation dictionnaire {groupe_name : [ids]}
            else:
                print("No groups found!")
                group = None
            
            logging.info('Connected to the gateway!')
            
            #creation du fichier d'info sur les devices
            for i in range(0, len(list(my_groups.keys()))):
                group =  { 'id': list(my_groups.keys())[i], 'lights': [] }
                for j in range(0, len(my_group_members)):
                    id = my_group_members[group['id']][j]
                    dimmer = API(gateway.get_device(id)).light_control.lights[0].dimmer
                    color = API(gateway.get_device(id)).light_control.lights[0].hex_color
                    light = { 'id' : id, 'dimmer':dimmer, 'color':color}
                    group['lights'].append(light)
                device_info['groups'].append(group)
            
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
