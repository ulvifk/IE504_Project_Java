import os
import json
import xmltodict
import random

def convert_xml_to_json(xml_file_path, json_file_path):

    # Read the XML file
    with open(xml_file_path, 'r') as file:
        xml_content = file.read()
    # Convert XML to JSON
    json_content = xmltodict.parse(xml_content)
    id_to_demand_map = {}
    if 'requests' in json_content.get('instance', {}):
        requests = json_content['instance']['requests']
        if 'request' in requests:
            for request in requests['request']:
                 request_id = request.get('@id')
                 if request_id:
                    demand = random.randint(0, 50)  # You can replace this with your desired logic for demand
                    id_to_demand_map[request_id] = demand
            #     # Replace service_time with demand
            #     request['demand'] = random.randint(0, 50)
            #     if 'service_time' in request:
            #         del request['service_time']
            # requests['requests'] = requests.pop('request')

    if 'network' in json_content.get('instance', {}):
        network = json_content['instance']['network']
        if 'nodes' in network and 'node' in network['nodes']:
            for node in network['nodes']['node']:
                # Check if the node is a recharge station and remove 'custom' attribute
                if node.get('@type') == '2' and 'custom' in node:  # Assuming type '2' is for recharge stations
                    del node['custom']
                    node['charging_rate'] = "500"
                if node.get('@type') == '1':
                    node_id = node.get('@id')
                    if node_id in id_to_demand_map:
                        node['@demand'] = id_to_demand_map[node_id]
            network['nodes'] = network['nodes']['node']

    if 'fleet' in json_content.get('instance', {}):
        fleet = json_content['instance']['fleet']
        vehicle_profile = fleet.get('vehicle_profile', {})

        # Set charging type to fast in custom section
        if 'custom' in vehicle_profile:
            vehicle_profile['custom']['@cs_type'] = 'fast'

        # Add capacity
        vehicle_profile['capacity'] = "100"

        # Calculate and rename max_travel_distance
        if 'max_travel_time' in vehicle_profile and 'custom' in vehicle_profile:
            battery_capacity = float(vehicle_profile['custom'].get('battery_capacity', 1))
            consumption_rate = float(vehicle_profile['custom'].get('consumption_rate', 1))
            speed_factor = float(vehicle_profile.get('speed_factor', 1))
            vehicle_profile['max_travel_distance'] = battery_capacity / consumption_rate * speed_factor
            del vehicle_profile['max_travel_time']  # Remove the old key if needed

          
    # Write the JSON file
    with open(json_file_path, 'w') as file:
        json.dump(json_content, file, indent=4)

def delete_json_files(directory):
    # delete json files
    for filename in os.listdir(directory):
        if filename.endswith(".json"):
            os.remove(os.path.join(directory, filename))

def convert_all_xml_to_json(directory):
    delete_json_files(directory)
    for filename in os.listdir(directory):
        if filename.endswith(".xml"):
            xml_file_path = os.path.join(directory, filename)
            json_file_path = os.path.join(directory, filename.replace('.xml', '.json'))
            convert_xml_to_json(xml_file_path, json_file_path)

directory = 'montoya-et-al-2017-xml'
convert_all_xml_to_json(directory)