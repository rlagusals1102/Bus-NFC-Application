import json

async def get_bus_gps(route_id: str, stId: str):
    try:
        with open('app/static/BusInfoData.json', 'r', encoding='utf-8') as file:
            data = json.load(file)

        for item in data:
            if item.get('ROUTE_ID') == route_id and item.get('NODE_ID') == stId:
                return {'x_point': item.get('X좌표'), 'y_point': item.get('Y좌표')}

        return {"message": "Unable to find coordinates"}

    except FileNotFoundError:
        return {"error": "File not found: BusInfoData.json"}

    except json.JSONDecodeError:
        return {"error": "Error decoding JSON data."}
