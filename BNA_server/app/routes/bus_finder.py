from fastapi import APIRouter, Query
from services.bus_finder_service import bus_finder
from utilities.deps import pattern
from utilities.exceptions import handle_exceptions

router = APIRouter()

@router.get("/bus_finder")
async def bus_finder_routes(
        route_id: str = Query(..., description="Route ID as a string", regex=pattern),
        stId: str = Query(..., description="Station ID as a string", regex=pattern)
):
    try:
        result = await bus_finder(route_id, stId)
        return result
    except Exception as e:
        raise handle_exceptions(e)
