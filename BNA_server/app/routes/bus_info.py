from fastapi import APIRouter, Query
from services.bus_info_service import get_bus_info
from utils.deps import pattern
from utils.deps import handle_exceptions
router = APIRouter()

@router.get("/bus_info")
async def get_bus_info_route(
        route_id: str = Query(..., description="Route ID as a string", regex=pattern),
        stId: str = Query(..., description="Station ID as a string", regex=pattern)
):
    try:
        return await get_bus_info(route_id, stId)
        
    except Exception as e:
        raise handle_exceptions(e)
