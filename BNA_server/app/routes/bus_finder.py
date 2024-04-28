from fastapi import APIRouter, Query
from services.bus_finder_service import bus_finder
from utils.deps import pattern
from utils.deps import handle_exceptions
router = APIRouter()

@router.get("/bus_finder")
async def bus_finder_routes(
        route_id: str = Query(..., description="Route ID as a string", regex = pattern),
        stId: str = Query(..., description="Station ID as a string", regex = pattern)
):
    try:
        return await bus_finder(route_id, stId)
    
    except Exception as e:
        raise handle_exceptions(e)
