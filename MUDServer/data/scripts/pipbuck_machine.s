{if:
    {gt:{prop:contents/pipbuck,{&this}},0},
    {do:
        {give:{&player},{create_item:mud.foe.pipbuck}},
        {tell:You cautiously stick your hoof into the hole.,{&player}}
    },
    {tell:Insufficient Pipbucks Available!,{&player}}
}