import httpx
import json
import xml.etree.ElementTree as ET
from app.utilities.keys import BUS_API_URL

async def bus_list(route_id: str, stId: str):
    url = f"{BUS_API_URL}&busRouteId={route_id}"

    async with httpx.AsyncClient() as client:
        response = await client.get(url)

    response_content = response.text

    try:
        root = ET.fromstring(response_content)
        stNm = None

        for itemList in root.findall('.//itemList'):
            if itemList.find('stId').text == stId:
                stNm = itemList.find('stNm').text

        if stNm:
            return await route_information(stNm, stId)
        else:
            return {"error": "Station not found in API response."}

    except ET.ParseError:
        return {"error": "Error parsing XML."}


async def route_information(name: str, stId: str):
    res = []

    try:
        file_path = "app/static/BusInfoData.json"
        with open(file_path, 'r', encoding='utf-8') as json_file:
            data = json.load(json_file)

            for item in data:
                if item.get('정류소명') == name and item.get('NODE_ID') == stId:
                    res.append(item.get('노선명'))

            if res:
                return list(res)
            else:
                return {"error": "No matching routes found in JSON data."}

    except FileNotFoundError:
        return {"error": f"File not found: {file_path}"}

    except json.JSONDecodeError:
        return {"error": f"Error decoding JSON data."}
