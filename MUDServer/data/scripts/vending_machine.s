{do:
    {set:money,{prop:money,{&this}}},
    {set:stock,{prop:inventory/sparkle_cola,{&this}}},
    {if:
        {ge:{&money},1},
        {if:
            {ge:{&stock},1},
            {do:
                {tell:{colors:green,Enough Money!},{&player}},
                {set:money,{sub:{&money},1}},
                {store:money,{&this},{&money}},
                {set:stock,{sub:{&stock},1}},
                {store:inventory/sparkle_cola,{&this},{&stock}},
                {tell:PCHING! A bottle of sparkle cola!,{&player}},
                {give:{&player},{create_item:mud.foe.sparkle_cola}}
            },
            {tell:Sold Out!,{&player}}
        },
        {tell:{colors:red,Insufficient Funds!},{&player}}
    }
}