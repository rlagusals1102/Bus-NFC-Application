pattern = r"^\d{9}$"

route_type_colors = {
    '1': '#00a0e9',
    '2': '#53b332',
    '3': '#0068b7',
    '4': '#53b332',
    '5': '#f2b70a',
    '6': '#e60012'
}

async def extract_bus_info(item):
    return {
        'routeType': item.find('routeType').text,
        'routeColor': route_type_colors.get(item.find('routeType').text, 'Unknown'),
        'rtNm': item.find('rtNm').text,
        'stId': item.find('stId').text,
        'stNm': item.find('stNm').text,
        'arrmsg1': item.find('arrmsg1').text,
        'arrmsg2': item.find('arrmsg2').text
    }
