import json


async def get_bus_gps(route_id: str, stId: str):
    with open('app/static/BusInfoData.json', 'r', encoding='utf-8') as file:
        data = json.load(file)

    for item in data:
        if item['ROUTE_ID'] == route_id and item['NODE_ID'] == stId:
            return {'x_point': item['X좌표'], 'y_point': item['Y좌표']}

    return {"message": "Unable to find coordinates"}
