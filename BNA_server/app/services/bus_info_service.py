"""
NOTE : 현재 위치 정류장 정보
"""

import httpx
import xml.etree.ElementTree as ET
from utils.keys import BUS_API_URL
from utils.deps import extract_bus_info

async def get_bus_info(route_id: str, stId: str):
    url = f"{BUS_API_URL}&busRouteId={route_id}"
    
    async with httpx.AsyncClient() as client:
        response = await client.get(url)

    response_content = response.text
    root = ET.fromstring(response_content)

    for itemList in root.findall('.//itemList'):
        if itemList.find('stId').text == stId:
            return await extract_bus_info(itemList)

    return {"error": "No matching data found."}

