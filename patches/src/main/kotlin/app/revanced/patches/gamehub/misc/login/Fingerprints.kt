package app.revanced.patches.gamehub.misc.login

import app.revanced.patcher.fingerprint
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val USER_MANAGER_CLASS = "Lcom/xj/common/user/UserManager;"

internal val getAvatarFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == USER_MANAGER_CLASS && method.name == "getAvatar"
    }
}

internal val getNicknameFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == USER_MANAGER_CLASS && method.name == "getNickname"
    }
}

internal val getTokenFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == USER_MANAGER_CLASS && method.name == "getToken"
    }
}

internal val getUidFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == USER_MANAGER_CLASS && method.name == "getUid"
    }
}

internal val getUsernameFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == USER_MANAGER_CLASS && method.name == "getUsername"
    }
}

internal val isLoginFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == USER_MANAGER_CLASS && method.name == "isLogin"
    }
}

// H: HomeLeftMenuDialog — method that builds the left menu item list, identified by the
// presence of the menu_user_center_normal drawable resource reference.
internal val homeLeftMenuDialogFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/ui/menu/HomeLeftMenuDialog;" &&
            method.implementation?.instructions?.any { instr ->
                instr.getReference<FieldReference>()?.name == "menu_user_center_normal"
            } == true
    }
}

// l1() is the avatar/username click handler in HomeLeftMenuDialog. It launches
// UserCenterActivity then dismisses the drawer. We strip the startActivity call
// so tapping the header just closes the drawer instead of opening User Center.
internal val homeLeftMenuAvatarClickFingerprint = fingerprint {
    custom { method, classDef ->
        classDef.type == "Lcom/xj/landscape/launcher/ui/menu/HomeLeftMenuDialog;" &&
            method.name == "l1"
    }
}
