from fastapi import APIRouter, Query
from BNA_server.app.services.bus_finder_service import bus_finder
from BNA_server.app.utilities.deps import pattern
from BNA_server.app.utilities.exceptions import handle_exceptions

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
