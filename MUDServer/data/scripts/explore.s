{do:
	{set:room,{prop:_game/id,{&this}}},
    {set:zone,{prop:_game/zone,{&this}}},
    {set:explored,{prop:{join:_game/explored,{join:{&zone},{&room},/},/},{&player}}},
    {if:
        {eq:{&explored},no},
        {do:
            {store:{join:_game/explored,{join:{&zone},{&room},/},/},{&player},yes},
            {tell:{colors:green,Exploration +1!},{&player}}
        },
        {store:_game/nothing,{&player},1}
    }
}