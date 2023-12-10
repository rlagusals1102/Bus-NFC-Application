from fastapi import FastAPI, Query
import json
import requests
import xml.etree.ElementTree as ET
import uvicorn
app = FastAPI()


def Route_Information(data, name):
    with open(data, 'r', encoding='utf-8') as json_file:
        bus_data = json.load(json_file)
    routes = {route['노선명'] for route in bus_data if route['정류소명'] == name}
    return list(routes)


def GPS(stId):
    file_path = 'static/BusInfoData.json'
    with open(file_path, 'r', encoding='utf-8') as file:
        data = json.load(file)
        for item in data:
            if item['NODE_ID'] == stId:
                x_coord = item['X좌표']
                y_coord = item['Y좌표']
                return x_coord, y_coord
        return None, None


@app.get('/get_list')
async def get_routes(name: str = Query(...)):
    data = 'static/BusInfoData.json'
    res_routes = Route_Information(data, name)
    return {'result_routes': res_routes}


@app.get('/get_routes')
async def get_bus_info(routes: str = Query(...), stId: str = Query(...)):
    url = 'http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll'
    params = {
        'serviceKey': 'WVI7ZtvAVX6hik3qi1Y37dBT8JHku9C+WhfM2MKgmcnMqJvckqqUdOpGAf9EpWdzA5gsaTyth86/Jnvo10Xxwg==',
        'busRouteId': routes
    }

    response = requests.get(url, params=params)
    root = ET.fromstring(response.text)[2]

    for i in range(len(root)):
        stId_node = root[i][75].text

        if stId_node == stId:
            idx1 = max(0, i - 1)
            idx2 = min(len(root) - 1, i + 1)

            return {
                'previous_stop_id': root[idx1][75].text,
                'previous_stop_name': root[idx1][76].text,
                'next_stop_id': root[idx2][75].text,
                'next_stop_name': root[idx2][76].text
            }
    return {}


@app.get('/get_ars_id_info')
async def get_ars_id_info(routes: str = Query(...), stId: str = Query(...)):
    url = 'http://ws.bus.go.kr/api/rest/arrive/getArrInfoByRouteAll'
    params = {
        'serviceKey': 'WVI7ZtvAVX6hik3qi1Y37dBT8JHku9C+WhfM2MKgmcnMqJvckqqUdOpGAf9EpWdzA5gsaTyth86/Jnvo10Xxwg==',
        'busRouteId': routes
    }

    response = requests.get(url, params=params)
    root = ET.fromstring(response.text)[2]

    for i in range(0, len(root)):
        stId_node = root[i][75].text

        if stId_node == stId:
            return {
                'route_info': root[i].findall("rtNm")[0].text,
                'first_arrival_msg': root[i].findall("arrmsg1")[0].text,
                'second_arrival_msg': root[i].findall("arrmsg2")[0].text,
                'first_bus_congestion': root[i].findall("brdrde_Num1")[0].text,
                'second_bus_congestion': root[i].findall("brdrde_Num2")[0].text,
                'route_type': root[i].findall("routeType")[0].text,
                'coordinates': GPS(stId)
                #노선유형 (1:공항, 2:마을, 3:간선, 4:지선, 5:순환, 6:광역, 7:인천, 8:경기, 9:폐지, 0:공용)
            }
    return {}
if __name__ == "__main__":
    host = "0.0.0.0"
    port = 8000
    uvicorn.run(app, host=host, port=port)