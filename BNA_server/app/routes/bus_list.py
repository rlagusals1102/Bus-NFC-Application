from fastapi import APIRouter, Query
from services.bus_list_service import bus_list
from utils.deps import pattern
from utils.deps import handle_exceptions
router = APIRouter()

@router.get("/bus_list")
async def bus_list_routes(
        route_id: str = Query(..., description="Route ID as a string", regex=pattern),
        stId: str = Query(..., description="Station ID as a string", regex=pattern)
):
    try:
        return await bus_list(route_id, stId)
        
    except Exception as e:
        raise handle_exceptions(e)
